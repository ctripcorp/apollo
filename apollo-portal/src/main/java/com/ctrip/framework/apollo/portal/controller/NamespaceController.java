package com.ctrip.framework.apollo.portal.controller;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.http.MultiResponseEntity;
import com.ctrip.framework.apollo.common.http.RichResponseEntity;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceCreationModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceDeletionEvent;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 名称空间 Controller
 */
@Slf4j
@RestController
public class NamespaceController {

  private final ApplicationEventPublisher publisher;
  private final UserInfoHolder userInfoHolder;
  private final NamespaceService namespaceService;
  private final AppNamespaceService appNamespaceService;
  private final RoleInitializationService roleInitializationService;
  private final PortalConfig portalConfig;
  private final PermissionValidator permissionValidator;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;

  public NamespaceController(
      final ApplicationEventPublisher publisher,
      final UserInfoHolder userInfoHolder,
      final NamespaceService namespaceService,
      final AppNamespaceService appNamespaceService,
      final RoleInitializationService roleInitializationService,
      final PortalConfig portalConfig,
      final PermissionValidator permissionValidator,
      final AdminServiceAPI.NamespaceAPI namespaceAPI) {
    this.publisher = publisher;
    this.userInfoHolder = userInfoHolder;
    this.namespaceService = namespaceService;
    this.appNamespaceService = appNamespaceService;
    this.roleInitializationService = roleInitializationService;
    this.portalConfig = portalConfig;
    this.permissionValidator = permissionValidator;
    this.namespaceAPI = namespaceAPI;
  }

  /**
   * 获取公有的应用名称空间列表
   *
   * @return 公有的应用名称空间列表
   */
  @GetMapping("/appnamespaces/public")
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceService.findPublicAppNamespaces();
  }

  /**
   * 查询名称空间列表
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 名称空间列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceBO> findNamespaces(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {

    List<NamespaceBO> namespaceBOs = namespaceService
        .findNamespaceBOs(appId, Env.valueOf(env), clusterName);

    for (NamespaceBO namespaceBO : namespaceBOs) {
      if (permissionValidator.shouldHideConfigToCurrentUser(appId, env,
          namespaceBO.getBaseInfo().getNamespaceName())) {
        namespaceBO.hideItems();
      }
    }

    return namespaceBOs;
  }

  /**
   * 查询指定的名称空间列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 名称空间列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
  public NamespaceBO findNamespace(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {

    NamespaceBO namespaceBO = namespaceService
        .loadNamespaceBO(appId, Env.valueOf(env), clusterName, namespaceName);

    if (namespaceBO != null && permissionValidator
        .shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      namespaceBO.hideItems();
    }

    return namespaceBO;
  }

  /**
   * 查询关联的名称空间公有名称空间
   *
   * @param appId         应用id
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @param clusterName   集群名称
   * @return 关联的名称空间公有名称空间
   */
  @GetMapping("/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace")
  public NamespaceBO findPublicNamespaceForAssociatedNamespace(@PathVariable String env,
      @PathVariable String appId, @PathVariable String namespaceName,
      @PathVariable String clusterName) {

    return namespaceService.findPublicNamespaceForAssociatedNamespace(Env.valueOf(env), appId,
        clusterName, namespaceName);
  }

  /**
   * 创建 Namespace
   * <p>
   * ps：关联 Namespace 也调用该接口
   *
   * @param appId  App 编号
   * @param models NamespaceCreationModel 数组
   * @return 成功
   */
  @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
  @PostMapping("/apps/{appId}/namespaces")
  public ResponseEntity<Void> createNamespace(@PathVariable String appId,
      @RequestBody List<NamespaceCreationModel> models) {
    // 校验 `models` 非空
    checkModel(!CollectionUtils.isEmpty(models));
    // 初始化 名称空间角色列表。
    String namespaceName = models.get(0).getNamespace().getNamespaceName();
    String operator = userInfoHolder.getUser().getUserId();

    roleInitializationService.initNamespaceRoles(appId, namespaceName, operator);
    roleInitializationService.initNamespaceEnvRoles(appId, namespaceName, operator);

    // 循环 `models` ，创建 Namespace 对象
    for (NamespaceCreationModel model : models) {
      NamespaceDTO namespace = model.getNamespace();
      // 校验相关参数非空
      RequestPrecondition.checkArgumentsNotEmpty(model.getEnv(), namespace.getAppId(),
          namespace.getClusterName(), namespace.getNamespaceName());

      // 创建 Namespace 对象
      try {
        namespaceService.createNamespace(Env.valueOf(model.getEnv()), namespace);
      } catch (Exception e) {
        log.error("create namespace fail.", e);
        Tracer.logError(String.format("create namespace fail. (env=%s namespace=%s)",
            model.getEnv(), namespace.getNamespaceName()), e);
      }
    }

    // 授予 Namespace Role 给当前管理员
    namespaceService.assignNamespaceRoleToOperator(appId, namespaceName,
        userInfoHolder.getUser().getUserId());

    return ResponseEntity.ok().build();
  }

  /**
   * 删除名称
   *
   * @param appId         应用id
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @param clusterName   集群名称
   * @return 删除成功，返回ResponseEntity.ok()
   */
  @PreAuthorize(value = "@permissionValidator.hasDeleteNamespacePermission(#appId)")
  @DeleteMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
  public ResponseEntity<Void> deleteNamespace(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {

    namespaceService.deleteNamespace(appId, Env.valueOf(env), clusterName, namespaceName);

    return ResponseEntity.ok().build();
  }

  /**
   * 删除名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 删除成功，返回ResponseEntity.ok()
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
  public ResponseEntity<Void> deleteAppNamespace(@PathVariable String appId,
      @PathVariable String namespaceName) {

    AppNamespace appNamespace = appNamespaceService.deleteAppNamespace(appId, namespaceName);
    // 发布 AppCreationEvent 删除事件
    publisher.publishEvent(new AppNamespaceDeletionEvent(appNamespace));
    return ResponseEntity.ok().build();
  }

  /**
   * 获取应用名称空间
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 应用名称空间信息
   */
  @GetMapping("/apps/{appId}/appnamespaces/{namespaceName:.+}")
  public AppNamespaceDTO findAppNamespace(@PathVariable String appId,
      @PathVariable String namespaceName) {
    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);

    if (appNamespace == null) {
      throw new BadRequestException(String.format(
          "AppNamespace not exists. AppId = %s, NamespaceName = %s", appId, namespaceName));
    }

    return BeanUtils.transform(AppNamespaceDTO.class, appNamespace);
  }

  /**
   * 创建应用名称空间
   *
   * @param appId                 App 编号
   * @param appendNamespacePrefix 追加的名称空间后缀
   * @param appNamespace          应用名称空间信息
   * @return 创建的 应用名称空间信息
   */
  @PreAuthorize(value = "@permissionValidator.hasCreateAppNamespacePermission(#appId, #appNamespace)")
  @PostMapping("/apps/{appId}/appnamespaces")
  public AppNamespace createAppNamespace(@PathVariable String appId,
      @RequestParam(defaultValue = "true") boolean appendNamespacePrefix,
      @Valid @RequestBody AppNamespace appNamespace) {

    // 校验 AppNamespace 的 `name` 格式正确。
    if (!InputValidator.isValidAppNamespace(appNamespace.getName())) {
      throw new BadRequestException(String.format("Invalid Namespace format: %s",
          InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE + " & "
              + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
    }
    // 保存 AppNamespace 对象到数据库
    AppNamespace createdAppNamespace = appNamespaceService
        .createAppNamespaceInLocal(appNamespace, appendNamespacePrefix);

    // 赋予权限，若满足如下任一条件：
    // 1. 公开类型的 AppNamespace 。
    // 2. 私有类型的 AppNamespace ，并且允许 App 管理员创建私有类型的 AppNamespace 。
    if (portalConfig.canAppAdminCreatePrivateNamespace() || createdAppNamespace.isPublic()) {
      //  授予名称空间角色
      namespaceService.assignNamespaceRoleToOperator(appId, appNamespace.getName(),
          userInfoHolder.getUser().getUserId());
    }
// 发布 AppNamespaceCreationEvent 创建事件
    publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));
    // 返回创建的 AppNamespace 对象
    return createdAppNamespace;
  }

  /**
   * 获取名称空间发布信息.
   * <p>env -> cluster -> 群集是否发布命名空间?
   * <p>Example: dev -> default -> true(默认群集为未发布命名空间)
   * <p>customCluster -> false (自定义集群的集群名称已发布)
   *
   * @param appId 应用id
   * @return 环境的发布信息，Map<环境,>
   */
  @GetMapping("/apps/{appId}/namespaces/publish_info")
  public Map<String, Map<String, Boolean>> getNamespacesPublishInfo(@PathVariable String appId) {
    return namespaceService.getNamespacesPublishInfo(appId);
  }

  /**
   * 获取公有应用名称空间的全部名称空间列表
   *
   * @param env                 环境
   * @param publicNamespaceName 公有的名称空间名称
   * @param page                页码
   * @param size                页面大小
   * @return 公有应用名称空间的全部名称空间列表
   */
  @GetMapping("/envs/{env}/appnamespaces/{publicNamespaceName}/namespaces")
  public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(@PathVariable String env,
      @PathVariable String publicNamespaceName,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    return namespaceService
        .getPublicAppNamespaceAllNamespaces(Env.valueOf(env), publicNamespaceName, page, size);

  }

  /**
   * 获取丢弃的名称空间列表
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 丢失的名称空间列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/missing-namespaces")
  public MultiResponseEntity<String> findMissingNamespaces(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName) {

    MultiResponseEntity<String> response = MultiResponseEntity.ok();

    Set<String> missingNamespaces = findMissingNamespaceNames(appId, env, clusterName);

    for (String missingNamespace : missingNamespaces) {
      response.addResponseEntity(RichResponseEntity.ok(missingNamespace));
    }

    return response;
  }

  /**
   * 创建丢弃的名称空间列表
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 创建丢弃的名称空间列表信息
   */
  @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/missing-namespaces")
  public ResponseEntity<Void> createMissingNamespaces(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName) {

    Set<String> missingNamespaces = findMissingNamespaceNames(appId, env, clusterName);

    // 批量创建
    for (String missingNamespace : missingNamespaces) {
      namespaceAPI.createMissingAppNamespace(Env.valueOf(env), findAppNamespace(appId,
          missingNamespace));
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 找到丢弃的名称空间列表
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 丢弃的名称空间名称列表
   */
  private Set<String> findMissingNamespaceNames(String appId, String env, String clusterName) {
    // 配置的应用名称空间列表
    List<AppNamespaceDTO> configDbAppNamespaces = namespaceAPI.getAppNamespaces(appId,
        Env.valueOf(env));
    // 配置的名称空间空间列表
    List<NamespaceDTO> configDbNamespaces = namespaceService.findNamespaces(appId, Env.valueOf(env),
        clusterName);
    // 界面的应用名称空间列表
    List<AppNamespace> portalDbAppNamespaces = appNamespaceService.findByAppId(appId);

    // 配置的应用名称空间名称列表
    Set<String> configDbAppNamespaceNames = configDbAppNamespaces.stream()
        .map(AppNamespaceDTO::getName).collect(Collectors.toSet());
    // 配置的名称空间名称列表
    Set<String> configDbNamespaceNames = configDbNamespaces.stream()
        .map(NamespaceDTO::getNamespaceName).collect(Collectors.toSet());

    // 界面所有的器应用名称空间名称列表
    Set<String> portalDbAllAppNamespaceNames = Sets.newHashSet();
    // 界面私有的应用名称空间名称列表
    Set<String> portalDbPrivateAppNamespaceNames = Sets.newHashSet();

    // 记录名称空间列表
    for (AppNamespace appNamespace : portalDbAppNamespaces) {
      portalDbAllAppNamespaceNames.add(appNamespace.getName());
      // 非公有就添加至私有
      if (!appNamespace.isPublic()) {
        portalDbPrivateAppNamespaceNames.add(appNamespace.getName());
      }
    }

    // AppNamespaces should be the same
    // 从AppNamespaces去除存在的名称
    // 差集 userIds的差集，如(userids[1,2],existedUserIds[2,3,4])  ---> 1
    Set<String> missingAppNamespaceNames = Sets.difference(portalDbAllAppNamespaceNames,
        configDbAppNamespaceNames);
    // 私有的名称必须全部都存在
    // 从AppNamespaces去除存在的名称
    // 差集 userIds的差集，如(userids[1,2],existedUserIds[2,3,4])  ---> 1
    Set<String> missingNamespaceNames = Sets.difference(portalDbPrivateAppNamespaceNames,
        configDbNamespaceNames);

    // 交集
    return Sets.union(missingAppNamespaceNames, missingNamespaceNames);
  }
}
