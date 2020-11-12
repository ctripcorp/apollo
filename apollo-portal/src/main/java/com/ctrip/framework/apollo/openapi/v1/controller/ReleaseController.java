package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.auth.ConsumerPermissionValidator;
import com.ctrip.framework.apollo.openapi.dto.NamespaceGrayDelReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.NamespaceBranchService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放API - 发布信息 Controller层
 */
@RestController("openapiReleaseController")
@RequestMapping("/openapi/v1/envs/{env}")
public class ReleaseController {

  private final ReleaseService releaseService;
  private final UserService userService;
  private final NamespaceBranchService namespaceBranchService;
  private final ConsumerPermissionValidator consumerPermissionValidator;

  public ReleaseController(
      final ReleaseService releaseService,
      final UserService userService,
      final NamespaceBranchService namespaceBranchService,
      final ConsumerPermissionValidator consumerPermissionValidator) {
    this.releaseService = releaseService;
    this.userService = userService;
    this.namespaceBranchService = namespaceBranchService;
    this.consumerPermissionValidator = consumerPermissionValidator;
  }

  /**
   * 创建名称空间发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param model         名称空间发布信息
   * @param request       请求对象
   * @return 创建的发布信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public OpenReleaseDTO createRelease(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @RequestBody NamespaceReleaseDTO model,
      HttpServletRequest request) {
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
            .getReleaseTitle()),
        "Params(releaseTitle and releasedBy) can not be empty");

    if (userService.findByUserId(model.getReleasedBy()) == null) {
      throw new BadRequestException("user(releaseBy) not exists");
    }

    // 创建
    NamespaceReleaseModel releaseModel = BeanUtils.transform(NamespaceReleaseModel.class, model);
    releaseModel.setAppId(appId);
    releaseModel.setEnv(Env.valueOf(env).toString());
    releaseModel.setClusterName(clusterName);
    releaseModel.setNamespaceName(namespaceName);

    return OpenApiBeanUtils.transformFromReleaseDTO(releaseService.publish(releaseModel));
  }

  /**
   * 加载最新的发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 最新的发布信息
   */
  @GetMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest")
  public OpenReleaseDTO loadLatestActiveRelease(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName,
      @PathVariable String namespaceName) {
    // 最新的发布信息
    ReleaseDTO releaseDTO = releaseService.loadLatestRelease(appId, Env.valueOf
        (env), clusterName, namespaceName);
    if (releaseDTO == null) {
      return null;
    }

    return OpenApiBeanUtils.transformFromReleaseDTO(releaseDTO);
  }

  /**
   * 全量发布
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名
   * @param deleteBranch  是否删除分支
   * @param model         名称空间发布信息
   * @param request       请求对象
   * @return 全量发布信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/merge")
  public OpenReleaseDTO merge(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @PathVariable String branchName,
      @RequestParam(value = "deleteBranch", defaultValue = "true") boolean deleteBranch,
      @RequestBody NamespaceReleaseDTO model, HttpServletRequest request) {
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
            .getReleaseTitle()),
        "Params(releaseTitle and releasedBy) can not be empty");

    if (userService.findByUserId(model.getReleasedBy()) == null) {
      throw new BadRequestException("user(releaseBy) not exists");
    }

    ReleaseDTO mergedRelease = namespaceBranchService.merge(appId, Env.valueOf(env.toUpperCase()),
        clusterName, namespaceName, branchName, model.getReleaseTitle(), model.getReleaseComment(),
        model.getIsEmergencyPublish(), deleteBranch, model.getReleasedBy());

    return OpenApiBeanUtils.transformFromReleaseDTO(mergedRelease);
  }

  /**
   * 灰度发布
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名
   * @param model         名称空间发布信息
   * @param request       请求对象
   * @return 创建的发布信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/releases")
  public OpenReleaseDTO createGrayRelease(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName,
      @PathVariable String namespaceName, @PathVariable String branchName,
      @RequestBody NamespaceReleaseDTO model, HttpServletRequest request) {
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(),
        model.getReleaseTitle()),
        "Params(releaseTitle and releasedBy) can not be empty");

    if (userService.findByUserId(model.getReleasedBy()) == null) {
      throw new BadRequestException("user(releaseBy) not exists");
    }

    NamespaceReleaseModel releaseModel = BeanUtils.transform(NamespaceReleaseModel.class, model);
    releaseModel.setAppId(appId);
    releaseModel.setEnv(Env.valueOf(env).toString());
    releaseModel.setClusterName(branchName);
    releaseModel.setNamespaceName(namespaceName);

    return OpenApiBeanUtils.transformFromReleaseDTO(releaseService.publish(releaseModel));
  }

  /**
   * 灰度删除发布
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param model         名称空间灰度删除发布信息
   * @param request       请求对象
   * @return 发布信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/gray-del-releases")
  public OpenReleaseDTO createGrayDelRelease(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName,
      @PathVariable String namespaceName, @PathVariable String branchName,
      @RequestBody NamespaceGrayDelReleaseDTO model,
      HttpServletRequest request) {
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
            .getReleaseTitle()),
        "Params(releaseTitle and releasedBy) can not be empty");
    RequestPrecondition.checkArguments(model.getGrayDelKeys() != null,
        "Params(grayDelKeys) can not be null");

    if (userService.findByUserId(model.getReleasedBy()) == null) {
      throw new BadRequestException("user(releaseBy) not exists");
    }

    NamespaceGrayDelReleaseModel releaseModel = BeanUtils
        .transform(NamespaceGrayDelReleaseModel.class, model);
    releaseModel.setAppId(appId);
    releaseModel.setEnv(env.toUpperCase());
    releaseModel.setClusterName(branchName);
    releaseModel.setNamespaceName(namespaceName);

    return OpenApiBeanUtils.transformFromReleaseDTO(
        releaseService.publish(releaseModel, releaseModel.getReleasedBy()));
  }

  /**
   * 回滚
   *
   * @param env       环境
   * @param releaseId 发布id
   * @param operator  操作者
   * @param request   请求对象
   */
  @PutMapping(path = "/releases/{releaseId}/rollback")
  public void rollback(@PathVariable String env,
      @PathVariable long releaseId, @RequestParam String operator, HttpServletRequest request) {
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(operator),
        "Param operator can not be empty");

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("user(operator) not exists");
    }

    // 发布信息
    ReleaseDTO release = releaseService.findReleaseById(Env.valueOf(env), releaseId);
    if (release == null) {
      throw new BadRequestException("release not found");
    }

    if (!consumerPermissionValidator.hasReleaseNamespacePermission(request, release.getAppId(),
        release.getNamespaceName(), env)) {
      throw new AccessDeniedException("Forbidden operation. you don't have release permission");
    }
    // 回滚
    releaseService.rollback(Env.valueOf(env), releaseId, operator);
  }
}
