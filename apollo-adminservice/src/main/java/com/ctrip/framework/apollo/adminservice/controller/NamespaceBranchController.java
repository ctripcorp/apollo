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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 名称空间的分支（子名称空间） Controller层
 */
@RestController
public class NamespaceBranchController {

  private final MessageSender messageSender;
  private final NamespaceBranchService namespaceBranchService;
  private final NamespaceService namespaceService;

  public NamespaceBranchController(
      final MessageSender messageSender,
      final NamespaceBranchService namespaceBranchService,
      final NamespaceService namespaceService) {
    this.messageSender = messageSender;
    this.namespaceBranchService = namespaceBranchService;
    this.namespaceService = namespaceService;
  }

  /**
   * 创建分支的子名称空间
   *
   * @param appId         应用名称
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @return 创建的分支子名称空间信息
   */
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public NamespaceDTO createBranch(@PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @RequestParam("operator") String operator) {
    // 查询名称空间是否存在
    checkNamespace(appId, clusterName, namespaceName);

    // 创建名称空间分支（子名称空间）
    Namespace createdBranch = namespaceBranchService.createBranch(appId, clusterName, namespaceName,
        operator);
    return BeanUtils.transform(NamespaceDTO.class, createdBranch);
  }

  /**
   * 查询名称空间分支（子名称空间）灰度发布规则信息
   *
   * @param appId         应用名称
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 灰度发布规则信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
  public GrayReleaseRuleDTO findBranchGrayRules(@PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @PathVariable String branchName) {
    // 检查分支下所有的名称空间是否存在
    checkBranch(appId, clusterName, namespaceName, branchName);

    // 名称空间分支(子名称空间)灰度发布规则
    GrayReleaseRule rules = namespaceBranchService
        .findBranchGrayRules(appId, clusterName, namespaceName, branchName);
    if (rules == null) {
      return null;
    }

    GrayReleaseRuleDTO ruleDTO = new GrayReleaseRuleDTO(rules.getAppId(), rules.getClusterName(),
        rules.getNamespaceName(), rules.getBranchName());
    ruleDTO.setReleaseId(rules.getReleaseId());
    ruleDTO.setRuleItems(GrayReleaseRuleItemTransformer.batchTransformFromJSON(rules.getRules()));

    return ruleDTO;
  }

  /**
   * 更新名称空间分支(子名称空间)灰度发布规则
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param newRuleDto    灰度发布规则信息
   */
  @Transactional(rollbackFor = Exception.class)
  @PutMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
  public void updateBranchGrayRules(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespaceName, @PathVariable String branchName,
      @RequestBody GrayReleaseRuleDTO newRuleDto) {

    // 检查分支下所有的名称空间是否存在
    checkBranch(appId, clusterName, namespaceName, branchName);

    GrayReleaseRule newRules = BeanUtils.transform(GrayReleaseRule.class, newRuleDto);
    newRules.setRules(GrayReleaseRuleItemTransformer.batchTransformToJSON(newRuleDto
        .getRuleItems()));
    newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);

    // 更新分支灰度发布规则
    namespaceBranchService.updateBranchGrayRules(appId, clusterName, namespaceName, branchName,
        newRules);
    // 发送消息
    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName,
        namespaceName), Topics.APOLLO_RELEASE_TOPIC);
  }

  /**
   * 删除分支
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param operator      操作人
   */
  @Transactional(rollbackFor = Exception.class)
  @DeleteMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}")
  public void deleteBranch(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespaceName, @PathVariable String branchName,
      @RequestParam("operator") String operator) {

    // 检查分支
    checkBranch(appId, clusterName, namespaceName, branchName);

    // 删除名称空间分支(子名称空间)
    namespaceBranchService.deleteBranch(appId, clusterName, namespaceName, branchName,
        NamespaceBranchStatus.DELETED, operator);

    // 发送消息
    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName,
        namespaceName), Topics.APOLLO_RELEASE_TOPIC);
  }

  /**
   * 查询名称空间分支信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间分支信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public NamespaceDTO loadNamespaceBranch(@PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespaceName) {
    // 查询父名称空间是否存在
    checkNamespace(appId, clusterName, namespaceName);
    // 加载名称空间的分支（子名称空间）
    Namespace childNamespace = namespaceBranchService.findBranch(appId, clusterName, namespaceName);
    if (childNamespace == null) {
      return null;
    }

    return BeanUtils.transform(NamespaceDTO.class, childNamespace);
  }

  /**
   * 检查分支下所有的名称空间是否存在
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   */
  private void checkBranch(String appId, String clusterName, String namespaceName,
      String branchName) {
    //1. 检查父名称空间
    checkNamespace(appId, clusterName, namespaceName);

    //2. 检查子名称空间
    Namespace childNamespace = namespaceService.findOne(appId, branchName, namespaceName);
    if (childNamespace == null) {
      throw new BadRequestException(String.format(
          "Namespace's branch not exist. AppId = %s, ClusterName = %s, NamespaceName = %s, BranchName = %s",
          appId, clusterName, namespaceName, branchName));
    }

  }

  /**
   * 查询名称空间是否存在
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   */
  private void checkNamespace(String appId, String clusterName, String namespaceName) {
    Namespace parentNamespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (parentNamespace == null) {
      throw new BadRequestException(String
          .format("Namespace not exist. AppId = %s, ClusterName = %s, NamespaceName = %s", appId,
              clusterName, namespaceName));
    }
  }
}