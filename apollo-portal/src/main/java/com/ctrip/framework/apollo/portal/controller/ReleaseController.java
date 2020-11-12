package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseBO;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.listener.ConfigPublishEvent;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布 Controller
 */
@Validated
@RestController
public class ReleaseController {

  private final ReleaseService releaseService;
  private final ApplicationEventPublisher publisher;
  private final PortalConfig portalConfig;
  private final PermissionValidator permissionValidator;
  private final UserInfoHolder userInfoHolder;

  public ReleaseController(
      final ReleaseService releaseService,
      final ApplicationEventPublisher publisher,
      final PortalConfig portalConfig,
      final PermissionValidator permissionValidator,
      final UserInfoHolder userInfoHolder) {
    this.releaseService = releaseService;
    this.publisher = publisher;
    this.portalConfig = portalConfig;
    this.permissionValidator = permissionValidator;
    this.userInfoHolder = userInfoHolder;
  }

  /**
   * 发布配置
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param model         名称空间发布对象
   * @return 保存的发布信息
   */
  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public ReleaseDTO createRelease(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceReleaseModel model) {
    // 设置 PathVariable 变量到 NamespaceReleaseModel 中
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    // 若是紧急发布，但是当前环境未允许该操作，抛出 BadRequestException 异常
    if (model.getIsEmergencyPublish() && !portalConfig.isEmergencyPublishAllowed(
        Env.valueOf(env))) {
      throw new BadRequestException(String.format("Env: %s is not supported emergency publish now",
          env));
    }
    // 发布配置
    ReleaseDTO createdRelease = releaseService.publish(model);
    // 创建 ConfigPublishEvent 对象
    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId).withCluster(clusterName).withNamespace(namespaceName)
        .withReleaseId(createdRelease.getId()).setNormalPublishEvent(true).setEnv(Env.valueOf(env));
    // 发布 ConfigPublishEvent 事件
    publisher.publishEvent(event);
    return createdRelease;
  }

  /**
   * 灰度发布
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @param model         名称空间发布对象
   * @return 灰度发布的信息
   */
  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/releases")
  public ReleaseDTO createGrayRelease(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName,
      @PathVariable String namespaceName, @PathVariable String branchName,
      @RequestBody NamespaceReleaseModel model) {
    // 设置 PathVariable 变量到 NamespaceReleaseModel 中
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(branchName);
    model.setNamespaceName(namespaceName);
    // 若是紧急发布，但是当前环境未允许该操作，抛出 BadRequestException 异常
    if (model.getIsEmergencyPublish() && !portalConfig.isEmergencyPublishAllowed(
        Env.valueOf(env))) {
      throw new BadRequestException(String.format("Env: %s is not supported emergency publish now",
          env));
    }
    // 发布配置
    ReleaseDTO createdRelease = releaseService.publish(model);
    // 创建 ConfigPublishEvent 对象
    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId)
        .withCluster(clusterName)
        .withNamespace(namespaceName)
        .withReleaseId(createdRelease.getId())
        .setGrayPublishEvent(true)
        .setEnv(Env.valueOf(env));
    // 发布 ConfigPublishEvent 事件
    publisher.publishEvent(event);

    return createdRelease;
  }

  /**
   * 获取发布信息
   *
   * @param env       环境
   * @param releaseId 发布信息id
   * @return 指定的发布信息
   */
  @GetMapping("/envs/{env}/releases/{releaseId}")
  public ReleaseDTO get(@PathVariable String env, @PathVariable long releaseId) {
    ReleaseDTO release = releaseService.findReleaseById(Env.valueOf(env), releaseId);

    if (release == null) {
      throw new NotFoundException("release not found");
    }
    return release;
  }

  /**
   * 获取所有的发布信息列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 所有的发布信息列表
   */
  @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all")
  public List<ReleaseBO> findAllReleases(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
      @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "5") int size) {
    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

    return releaseService
        .findAllReleases(appId, Env.valueOf(env), clusterName, namespaceName, page, size);
  }

  /**
   * 最新的发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 获取最新的发布信息列表
   */
  @GetMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
  public List<ReleaseDTO> findActiveReleases(@PathVariable String appId,
      @PathVariable String env,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
      @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "5") int size) {

    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

    return releaseService
        .findActiveReleases(appId, Env.valueOf(env), clusterName, namespaceName, page, size);
  }

  /**
   * 比较发布信息
   *
   * @param env                环境
   * @param baseReleaseId      基发布id
   * @param toCompareReleaseId 待比较发布id
   * @return 发布信息比较结果
   */
  @GetMapping(value = "/envs/{env}/releases/compare")
  public ReleaseCompareResult compareRelease(@PathVariable String env,
      @RequestParam long baseReleaseId,
      @RequestParam long toCompareReleaseId) {

    return releaseService.compare(Env.valueOf(env), baseReleaseId, toCompareReleaseId);
  }

  /**
   * 回滚
   *
   * @param env         环境
   * @param releaseId   发布id
   * @param toReleaseId 待回滚至的发布id
   */
  @PutMapping(path = "/envs/{env}/releases/{releaseId}/rollback")
  public void rollback(@PathVariable String env, @PathVariable long releaseId,
      @RequestParam(defaultValue = "-1") long toReleaseId) {
    ReleaseDTO release = releaseService.findReleaseById(Env.valueOf(env), releaseId);

    if (release == null) {
      throw new NotFoundException("release not found");
    }

    if (!permissionValidator.hasReleaseNamespacePermission(release.getAppId(),
        release.getNamespaceName(), env)) {
      throw new AccessDeniedException("Access is denied");
    }

    // 回滚
    if (toReleaseId > -1) {
      releaseService.rollbackTo(Env.valueOf(env), releaseId, toReleaseId, userInfoHolder.getUser()
          .getUserId());
    } else {
      releaseService.rollback(Env.valueOf(env), releaseId, userInfoHolder.getUser().getUserId());
    }

    // 创建 ConfigPublishEvent 对象
    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(release.getAppId()).withCluster(release.getClusterName())
        .withNamespace(release.getNamespaceName()).withPreviousReleaseId(releaseId)
        .setRollbackEvent(true).setEnv(Env.valueOf(env));
    // 发布 ConfigPublishEvent 事件
    publisher.publishEvent(event);
  }
}
