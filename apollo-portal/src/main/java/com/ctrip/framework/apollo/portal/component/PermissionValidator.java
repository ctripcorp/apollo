package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("permissionValidator")
public class PermissionValidator {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private RolePermissionService rolePermissionService;
  @Autowired
  private PortalConfig portalConfig;

  public boolean hasModifyNamespacePermission(String appId, String namespaceName, String env) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.MODIFY_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));
  }

  public boolean hasReleaseNamespacePermission(String appId, String namespaceName, String env) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.RELEASE_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));
  }

  public boolean hasDeleteNamespacePermission(String appId) {
    return hasAssignRolePermission(appId) || isSuperAdmin();
  }

  public boolean hasOperateNamespacePermission(String appId, String namespaceName, String env) {
    return hasModifyNamespacePermission(appId, namespaceName, env) || hasReleaseNamespacePermission(appId, namespaceName, env);
  }

  public boolean hasAssignRolePermission(String appId) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.ASSIGN_ROLE,
        appId);
  }

  public boolean hasCreateNamespacePermission(String appId) {

    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.CREATE_NAMESPACE,
        appId);
  }

  public boolean hasCreateAppNamespacePermission(String appId, AppNamespace appNamespace) {

    boolean isPublicAppNamespace = appNamespace.isPublic();

    if (portalConfig.canAppAdminCreatePrivateNamespace() || isPublicAppNamespace) {
      return hasCreateNamespacePermission(appId);
    }

    return isSuperAdmin();
  }

  public boolean hasCreateClusterPermission(String appId) {
    return rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(),
        PermissionType.CREATE_CLUSTER,
        appId);
  }

  public boolean isAppAdmin(String appId) {
    return isSuperAdmin() || hasAssignRolePermission(appId);
  }

  public boolean isSuperAdmin() {
    return rolePermissionService.isSuperAdmin(userInfoHolder.getUser().getUserId());
  }

  public boolean alwaysTrue() {
    return true;
  }
}
