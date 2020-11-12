package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.service.SystemRoleManagerService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限校验器
 */
@Component("permissionValidator")
public class PermissionValidator {

  private final UserInfoHolder userInfoHolder;
  private final RolePermissionService rolePermissionService;
  private final PortalConfig portalConfig;
  private final AppNamespaceService appNamespaceService;
  private final SystemRoleManagerService systemRoleManagerService;

  @Autowired
  public PermissionValidator(
      final UserInfoHolder userInfoHolder,
      final RolePermissionService rolePermissionService,
      final PortalConfig portalConfig,
      final AppNamespaceService appNamespaceService,
      final SystemRoleManagerService systemRoleManagerService) {
    this.userInfoHolder = userInfoHolder;
    this.rolePermissionService = rolePermissionService;
    this.portalConfig = portalConfig;
    this.appNamespaceService = appNamespaceService;
    this.systemRoleManagerService = systemRoleManagerService;
  }

  /**
   * 是否有修改名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 有权限，否则，false
   */
  public boolean hasModifyNamespacePermission(String appId, String namespaceName) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.MODIFY_NAMESPACE, RoleUtils.buildNamespaceTargetId(appId, namespaceName));
  }

  /**
   * 是否有修改名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return true, 有权限，否则，false
   */
  public boolean hasModifyNamespacePermission(String appId, String namespaceName, String env) {
    return hasModifyNamespacePermission(appId, namespaceName) ||
        rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
            PermissionType.MODIFY_NAMESPACE,
            RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));
  }

  /**
   * 是否有发布名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 有权限，否则，false
   */
  public boolean hasReleaseNamespacePermission(String appId, String namespaceName) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.RELEASE_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName));
  }

  /**
   * 是否有发布名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return true, 有权限，否则，false
   */
  public boolean hasReleaseNamespacePermission(String appId, String namespaceName, String env) {
    return hasReleaseNamespacePermission(appId, namespaceName) ||
        rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
            PermissionType.RELEASE_NAMESPACE,
            RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));
  }

  /**
   * 是否有删除名称空间的权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasDeleteNamespacePermission(String appId) {
    return hasAssignRolePermission(appId) || isSuperAdmin();
  }

  /**
   * 是否有操作名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 有权限，否则，false
   */
  public boolean hasOperateNamespacePermission(String appId, String namespaceName) {
    return hasModifyNamespacePermission(appId, namespaceName) || hasReleaseNamespacePermission(
        appId, namespaceName);
  }

  /**
   * 是否有操作名称空间的权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return true, 有权限，否则，false
   */
  public boolean hasOperateNamespacePermission(String appId, String namespaceName, String env) {
    return hasOperateNamespacePermission(appId, namespaceName) ||
        hasModifyNamespacePermission(appId, namespaceName, env) ||
        hasReleaseNamespacePermission(appId, namespaceName, env);
  }

  /**
   * 是否有分配角色的权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasAssignRolePermission(String appId) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.ASSIGN_ROLE, appId);
  }

  /**
   * 是否有创建名称空间的权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasCreateNamespacePermission(String appId) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.CREATE_NAMESPACE, appId);
  }

  /**
   * 是否有创建应用名称空间的权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasCreateAppNamespacePermission(String appId, AppNamespace appNamespace) {
    boolean isPublicAppNamespace = appNamespace.isPublic();
    // 若满足如下任一条件：
    // 1. 公开类型的 AppNamespace 。
    // 2. 私有类型的 AppNamespace ，并且允许 App 管理员创建私有类型的 AppNamespace 。
    if (portalConfig.canAppAdminCreatePrivateNamespace() || isPublicAppNamespace) {
      return hasCreateNamespacePermission(appId);
    }
    // 超管
    return isSuperAdmin();
  }

  /**
   * 是否有创建集群的权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasCreateClusterPermission(String appId) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.CREATE_CLUSTER, appId);
  }

  /**
   * 是否为应用管理人
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean isAppAdmin(String appId) {
    return isSuperAdmin() || hasAssignRolePermission(appId);
  }

  /**
   * 是否为超级管理员
   *
   * @return 如果为超级管理员, true, 否则，false
   */
  public boolean isSuperAdmin() {
    return rolePermissionService.isSuperAdmin(userInfoHolder.getUser().getUserId());
  }

  /**
   * 是否应该对当前用户隐藏配置
   *
   * @return 隐藏, true, 否则，false
   */
  public boolean shouldHideConfigToCurrentUser(String appId, String env, String namespaceName) {
    // 1. check whether the current environment enables member only function
    if (!portalConfig.isConfigViewMemberOnly(env)) {
      return false;
    }

    // 2. public namespace is open to every one
    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);
    if (appNamespace != null && appNamespace.isPublic()) {
      return false;
    }

    // 3. check app admin and operate permissions
    return !isAppAdmin(appId) && !hasOperateNamespacePermission(appId, namespaceName, env);
  }

  /**
   * 当前用户是否有创建应用的权限
   *
   * @return true, 有权限，否则，false
   */
  public boolean hasCreateApplicationPermission() {
    return hasCreateApplicationPermission(userInfoHolder.getUser().getUserId());
  }

  /**
   * 是否有创建应用的权限
   *
   * @param userId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasCreateApplicationPermission(String userId) {
    return systemRoleManagerService.hasCreateApplicationPermission(userId);
  }

  /**
   * 是否有管理应用Master权限
   *
   * @param appId 应用id
   * @return true, 有权限，否则，false
   */
  public boolean hasManageAppMasterPermission(String appId) {
    // the manage app master permission might not be initialized, so we need to check isSuperAdmin first
    return isSuperAdmin() || (hasAssignRolePermission(appId) && systemRoleManagerService
        .hasManageAppMasterPermission(userInfoHolder.getUser().getUserId(), appId)
    );
  }
}
