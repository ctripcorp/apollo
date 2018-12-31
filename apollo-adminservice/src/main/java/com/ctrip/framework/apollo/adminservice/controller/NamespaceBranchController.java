package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.message.MessageSender;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.GrayReleaseRuleItemTransformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NamespaceBranchController {

  @Autowired
  private MessageSender messageSender;
  @Autowired
  private NamespaceBranchService namespaceBranchService;
  @Autowired
  private NamespaceService namespaceService;


  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.POST)
  public NamespaceDTO createBranch(@PathVariable String appId,
                                   @PathVariable String clusterName,
                                   @PathVariable String namespaceName,
                                   @RequestParam("operator") String operator) {

    checkNamespace(appId, clusterName, namespaceName);

    Namespace createdBranch = namespaceBranchService.createBranch(appId, clusterName, namespaceName, operator);

    return BeanUtils.transform(NamespaceDTO.class, createdBranch);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
      method = RequestMethod.GET)
  public GrayReleaseRuleDTO findBranchGrayRules(@PathVariable String appId,
                                                @PathVariable String clusterName,
                                                @PathVariable String namespaceName,
                                                @PathVariable String branchName) {

    checkBranch(appId, clusterName, namespaceName, branchName);

    GrayReleaseRule rules = namespaceBranchService.findBranchGrayRules(appId, clusterName, namespaceName, branchName);
    if (rules == null) {
      return null;
    }
    GrayReleaseRuleDTO ruleDTO =
        new GrayReleaseRuleDTO(rules.getAppId(), rules.getClusterName(), rules.getNamespaceName(),
                               rules.getBranchName());

    ruleDTO.setReleaseId(rules.getReleaseId());

    ruleDTO.setRuleItems(GrayReleaseRuleItemTransformer.batchTransformFromJSON(rules.getRules()));

    return ruleDTO;
  }

  @Transactional
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.PUT)
  public void updateBranchGrayRules(@PathVariable String appId, @PathVariable String clusterName,
                                    @PathVariable String namespaceName, @PathVariable String branchName,
                                    @RequestBody GrayReleaseRuleDTO newRuleDto) {

    checkBranch(appId, clusterName, namespaceName, branchName);

    GrayReleaseRule newRules = BeanUtils.transform(GrayReleaseRule.class, newRuleDto);
    newRules.setRules(GrayReleaseRuleItemTransformer.batchTransformToJSON(newRuleDto.getRuleItems()));
    newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);

    namespaceBranchService.updateBranchGrayRules(appId, clusterName, namespaceName, branchName, newRules);

    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                              Topics.APOLLO_RELEASE_TOPIC);
  }

  @Transactional
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}", method = RequestMethod.DELETE)
  public void deleteBranch(@PathVariable String appId, @PathVariable String clusterName,
                           @PathVariable String namespaceName, @PathVariable String branchName,
                           @RequestParam("operator") String operator) {

    checkBranch(appId, clusterName, namespaceName, branchName);

    namespaceBranchService
        .deleteBranch(appId, clusterName, namespaceName, branchName, NamespaceBranchStatus.DELETED, operator);

    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                              Topics.APOLLO_RELEASE_TOPIC);

  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.GET)
  public NamespaceDTO loadNamespaceBranch(@PathVariable String appId, @PathVariable String clusterName,
                                          @PathVariable String namespaceName) {

    checkNamespace(appId, clusterName, namespaceName);

    Namespace childNamespace = namespaceBranchService.findBranch(appId, clusterName, namespaceName);
    if (childNamespace == null) {
      return null;
    }

    return BeanUtils.transform(NamespaceDTO.class, childNamespace);
  }

  private void checkBranch(String appId, String clusterName, String namespaceName, String branchName) {
    //1. check parent namespace
    checkNamespace(appId, clusterName, namespaceName);

    //2. check child namespace
    Namespace childNamespace = namespaceService.findOne(appId, branchName, namespaceName);
    if (childNamespace == null) {
      throw new BadRequestException(String.format("Namespace's branch not exist. AppId = %s, ClusterName = %s, "
                                                  + "NamespaceName = %s, BranchName = %s",
                                                  appId, clusterName, namespaceName, branchName));
    }

  }

  private void checkNamespace(String appId, String clusterName, String namespaceName) {
    Namespace parentNamespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (parentNamespace == null) {
      throw new BadRequestException(String.format("Namespace not exist. AppId = %s, ClusterName = %s, NamespaceName = %s", appId,
                                                  clusterName, namespaceName));
    }
  }


}
