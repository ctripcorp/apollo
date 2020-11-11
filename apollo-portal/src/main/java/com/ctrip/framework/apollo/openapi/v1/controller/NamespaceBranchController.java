package com.ctrip.framework.apollo.openapi.v1.controller;


import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.auth.ConsumerPermissionValidator;
import com.ctrip.framework.apollo.openapi.dto.OpenGrayReleaseRuleDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.NamespaceBranchService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放API - 名称空间分支 Controller层
 *
 * @author qianjie  8/10/17
 */
@RestController("openapiNamespaceBranchController")
@RequestMapping("/openapi/v1/envs/{env}")
public class NamespaceBranchController {

  private final ConsumerPermissionValidator consumerPermissionValidator;
  private final ReleaseService releaseService;
  private final NamespaceBranchService namespaceBranchService;
  private final UserService userService;

  public NamespaceBranchController(
      final ConsumerPermissionValidator consumerPermissionValidator,
      final ReleaseService releaseService,
      final NamespaceBranchService namespaceBranchService,
      final UserService userService) {
    this.consumerPermissionValidator = consumerPermissionValidator;
    this.releaseService = releaseService;
    this.namespaceBranchService = namespaceBranchService;
    this.userService = userService;
  }

  /**
   * 查询分支名称空间信息
   *
   * @param appId         应用api
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 开放Api的名称空间信息
   */
  @GetMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public OpenNamespaceDTO findBranch(@PathVariable String appId,
      @PathVariable String env,
      @PathVariable String clusterName,
      @PathVariable String namespaceName) {
    // 名称空间信息
    NamespaceBO namespaceBO = namespaceBranchService.findBranch(appId,
        Env.valueOf(env.toUpperCase()), clusterName, namespaceName);
    if (namespaceBO == null) {
      return null;
    }
    return OpenApiBeanUtils.transformFromNamespaceBO(namespaceBO);
  }

  /**
   * 创建分支信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @param request       请求对象
   * @return 开放Api的名称空间信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasCreateNamespacePermission(#request, #appId)")
  @PostMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public OpenNamespaceDTO createBranch(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("operator") String operator, HttpServletRequest request) {
    // 操作人不能为空
    RequestPrecondition
        .checkArguments(!StringUtils.isContainEmpty(operator), "operator can not be empty");

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("operator " + operator + " not exists");
    }
    // 创建分支信息
    NamespaceDTO namespaceDTO = namespaceBranchService.createBranch(appId,
        Env.valueOf(env.toUpperCase()), clusterName, namespaceName, operator);
    if (namespaceDTO == null) {
      return null;
    }
    return BeanUtils.transform(OpenNamespaceDTO.class, namespaceDTO);
  }

  /**
   * 删除名称空间分支信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   * @param request       请求对象
   * @return 开放Api的名称空间信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName, #env)")
  @DeleteMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}")
  public void deleteBranch(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @PathVariable String branchName, @RequestParam("operator") String operator,
      HttpServletRequest request) {

    // 操作人不能为空
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(operator),
        "operator can not be empty");

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("operator " + operator + " not exists");
    }

    // 能否删除，判断权限
    boolean canDelete = consumerPermissionValidator.hasReleaseNamespacePermission(request, appId,
        namespaceName, env) || (consumerPermissionValidator.hasModifyNamespacePermission(request,
        appId, namespaceName, env) && releaseService.loadLatestRelease(appId, Env.valueOf(env),
        branchName, namespaceName) == null);

    if (!canDelete) {
      throw new AccessDeniedException("Forbidden operation. "
          + "Caused by: 1.you don't have release permission "
          + "or 2. you don't have modification permission "
          + "or 3. you have modification permission but branch has been released");
    }
    // 删除名称空间分支信息
    namespaceBranchService.deleteBranch(appId, Env.valueOf(env.toUpperCase()), clusterName,
        namespaceName, branchName, operator);
  }

  /**
   * 获取分支发布规则信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 分支发布规则信息
   */
  @GetMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
  public OpenGrayReleaseRuleDTO getBranchGrayRules(@PathVariable String appId,
      @PathVariable String env,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @PathVariable String branchName) {
    // 分支发布规则信息
    GrayReleaseRuleDTO grayReleaseRuleDTO = namespaceBranchService.findBranchGrayRules(appId,
        Env.valueOf(env.toUpperCase()), clusterName, namespaceName, branchName);
    if (grayReleaseRuleDTO == null) {
      return null;
    }
    return OpenApiBeanUtils.transformFromGrayReleaseRuleDTO(grayReleaseRuleDTO);
  }

  /**
   * 更新分支发布规则信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param rules         灰度发布规则信息
   * @return 分支发布规则信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName, #env)")
  @PutMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
  public void updateBranchRules(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @PathVariable String branchName, @RequestBody OpenGrayReleaseRuleDTO rules,
      @RequestParam("operator") String operator, HttpServletRequest request) {
    // 操作人不能为空
    RequestPrecondition
        .checkArguments(!StringUtils.isContainEmpty(operator), "operator can not be empty");

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("operator " + operator + " not exists");
    }

    rules.setAppId(appId);
    rules.setClusterName(clusterName);
    rules.setNamespaceName(namespaceName);
    rules.setBranchName(branchName);

    // 更新
    GrayReleaseRuleDTO grayReleaseRuleDTO = OpenApiBeanUtils.transformToGrayReleaseRuleDTO(rules);
    namespaceBranchService.updateBranchGrayRules(appId, Env.valueOf(env.toUpperCase()),
        clusterName, namespaceName, branchName, grayReleaseRuleDTO, operator);

  }
}
