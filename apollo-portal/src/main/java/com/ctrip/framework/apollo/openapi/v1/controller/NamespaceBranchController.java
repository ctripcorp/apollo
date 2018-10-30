package com.ctrip.framework.apollo.openapi.v1.controller;

/**
 * Created by qianjie on 8/10/17.
 */

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.auth.ConsumerPermissionValidator;
import com.ctrip.framework.apollo.openapi.dto.OpenGrayReleaseRuleDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.service.NamespaceBranchService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("openapiNamespaceBranchController")
@RequestMapping("/openapi/v1/envs/{env}")
public class NamespaceBranchController {

    @Autowired
    private ConsumerPermissionValidator consumerPermissionValidator;
    @Autowired
    private ReleaseService releaseService;
    @Autowired
    private NamespaceBranchService namespaceBranchService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.GET)
    public OpenNamespaceDTO findBranch(@PathVariable String appId,
                                       @PathVariable String env,
                                       @PathVariable String clusterName,
                                       @PathVariable String namespaceName) {
        NamespaceBO namespaceBO = namespaceBranchService.findBranch(appId, Env.valueOf(env.toUpperCase()), clusterName, namespaceName);
        if (namespaceBO == null) {
            return null;
        }
        return OpenApiBeanUtils.transformFromNamespaceBO(namespaceBO);
    }

    @PreAuthorize(value = "@consumerPermissionValidator.hasCreateNamespacePermission(#request, #appId)")
    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.POST)
    public OpenNamespaceDTO createBranch(@PathVariable String appId,
                                         @PathVariable String env,
                                         @PathVariable String clusterName,
                                         @PathVariable String namespaceName,
                                         @RequestParam("operator") String operator,
                                         HttpServletRequest request) {
        RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(operator),"operator can not be empty");

        if (userService.findByUserId(operator) == null) {
            throw new BadRequestException("operator " + operator + " not exists");
        }

        NamespaceDTO namespaceDTO = namespaceBranchService.createBranch(appId, Env.valueOf(env.toUpperCase()), clusterName, namespaceName, operator);
        if (namespaceDTO == null) {
            return null;
        }
        return BeanUtils.transfrom(OpenNamespaceDTO.class, namespaceDTO);
    }

    @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName, #env)")
    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}", method = RequestMethod.DELETE)
    public void deleteBranch(@PathVariable String appId,
                             @PathVariable String env,
                             @PathVariable String clusterName,
                             @PathVariable String namespaceName,
                             @PathVariable String branchName,
                             @RequestParam("operator") String operator,
                             HttpServletRequest request) {
        RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(operator),"operator can not be empty");

        if (userService.findByUserId(operator) == null) {
            throw new BadRequestException("operator " + operator + " not exists");
        }

        boolean canDelete = consumerPermissionValidator.hasReleaseNamespacePermission(request, appId, namespaceName, env) ||
            (consumerPermissionValidator.hasModifyNamespacePermission(request, appId, namespaceName, env) &&
                releaseService.loadLatestRelease(appId, Env.valueOf(env), branchName, namespaceName) == null);

        if (!canDelete) {
            throw new AccessDeniedException("Forbidden operation. "
                + "Caused by: 1.you don't have release permission "
                + "or 2. you don't have modification permission "
                + "or 3. you have modification permission but branch has been released");
        }
        namespaceBranchService.deleteBranch(appId, Env.valueOf(env.toUpperCase()), clusterName, namespaceName, branchName, operator);

    }

    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.GET)
    public OpenGrayReleaseRuleDTO getBranchGrayRules(@PathVariable String appId, @PathVariable String env,
                                                     @PathVariable String clusterName,
                                                     @PathVariable String namespaceName,
                                                     @PathVariable String branchName) {
        GrayReleaseRuleDTO grayReleaseRuleDTO = namespaceBranchService.findBranchGrayRules(appId, Env.valueOf(env.toUpperCase()), clusterName, namespaceName, branchName);
        if (grayReleaseRuleDTO == null) {
            return null;
        }
        return OpenApiBeanUtils.transformFromGrayReleaseRuleDTO(grayReleaseRuleDTO);
    }

    @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName, #env)")
    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.PUT)
    public void updateBranchRules(@PathVariable String appId, @PathVariable String env,
                                  @PathVariable String clusterName, @PathVariable String namespaceName,
                                  @PathVariable String branchName, @RequestBody OpenGrayReleaseRuleDTO rules,
                                  @RequestParam("operator") String operator,
                                  HttpServletRequest request) {
        RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(operator),"operator can not be empty");

        if (userService.findByUserId(operator) == null) {
            throw new BadRequestException("operator " + operator + " not exists");
        }

        rules.setAppId(appId);
        rules.setClusterName(clusterName);
        rules.setNamespaceName(namespaceName);
        rules.setBranchName(branchName);

        GrayReleaseRuleDTO grayReleaseRuleDTO = OpenApiBeanUtils.transformToGrayReleaseRuleDTO(rules);
        namespaceBranchService
                .updateBranchGrayRules(appId, Env.valueOf(env.toUpperCase()), clusterName, namespaceName, branchName, grayReleaseRuleDTO, operator);

    }
}
