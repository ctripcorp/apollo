package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 初始化角色
 * Created by timothy on 2017/4/26.
 */
public class DefaultRoleInitializationService implements RoleInitializationService {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private RolePermissionService rolePermissionService;
  @Autowired
  private PortalConfig portalConfig;

  /**
   * 初始化应用角色
   * @param app
   */
  @Override
  @Transactional
  public void initAppRoles(App app) {

    String appId = app.getAppId();
    // 创建 App 拥有者的角色名
    String appMasterRoleName = RoleUtils.buildAppMasterRoleName(appId);

    // 校验角色是否已经存在。若是，直接返回
    if (rolePermissionService.findRoleByRoleName(appMasterRoleName) != null) {
      return;
    }
    String operator = app.getDataChangeCreatedBy();

    //create app permissions
    createAppMasterRole(appId, operator);

    // 初始化查看角色
    createAppViewerRole(appId, operator);
    List<Env> portalEnvs = portalConfig.portalSupportedEnvs();
    for (Env env : portalEnvs) {
      // 根据环境初始化查看角色
      createAppViewerEnvRole(appId, operator, env.toString());
    }


    //assign master role to user
    rolePermissionService.assignRoleToUsers(RoleUtils.buildAppMasterRoleName(appId), Sets.newHashSet(app.getOwnerName()), operator);
    // 授予查看角色
    rolePermissionService.assignRoleToUsers(RoleUtils.buildViewerAppRoleName(appId), Sets.newHashSet(app.getOwnerName()), operator);

    initNamespaceRoles(appId, ConfigConsts.NAMESPACE_APPLICATION, operator);
    initNamespaceEnvRoles(appId, ConfigConsts.NAMESPACE_APPLICATION, operator);

    //assign modify、release namespace role to user
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.MODIFY_NAMESPACE), Sets.newHashSet(operator), operator);
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.RELEASE_NAMESPACE), Sets.newHashSet(operator), operator);

  }


  /**
   * 初始化配置角色
   * @param appId
   * @param namespaceName
   * @param operator
   */
  @Override
  @Transactional
  public void initNamespaceRoles(String appId, String namespaceName, String operator) {

    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName) == null) {
      createNamespaceRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE,
          modifyNamespaceRoleName, operator);
    }

    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName) == null) {
      createNamespaceRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE,
          releaseNamespaceRoleName, operator);
    }
  }

  @Override
  @Transactional
  public void initNamespaceEnvRoles(String appId, String namespaceName, String operator) {
    List<Env> portalEnvs = portalConfig.portalSupportedEnvs();

    for (Env env : portalEnvs) {
      initNamespaceSpecificEnvRoles(appId, namespaceName, env.toString(), operator);
    }
  }

  @Override
  @Transactional
  public void initNamespaceSpecificEnvRoles(String appId, String namespaceName, String env, String operator) {
    String modifyNamespaceEnvRoleName = RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName, env);
    if (rolePermissionService.findRoleByRoleName(modifyNamespaceEnvRoleName) == null) {
      createNamespaceEnvRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE, env,
          modifyNamespaceEnvRoleName, operator);
    }

    String releaseNamespaceEnvRoleName = RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName, env);
    if (rolePermissionService.findRoleByRoleName(releaseNamespaceEnvRoleName) == null) {
      createNamespaceEnvRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE, env,
          releaseNamespaceEnvRoleName, operator);
    }

    String viewerEnvRoleName = RoleUtils.buildViewerAppEnvRoleName(appId, env);
    if(rolePermissionService.findRoleByRoleName(viewerEnvRoleName) == null) {
      createAppViewerEnvRole(appId, operator, env);
    }
  }

  private void createAppMasterRole(String appId, String operator) {
    Set<Permission> appPermissions = FluentIterable.from(Lists.newArrayList(
            PermissionType.CREATE_CLUSTER, PermissionType.CREATE_NAMESPACE, PermissionType.ASSIGN_ROLE))
            .transform(permissionType -> createPermission(appId, permissionType, operator)).toSet();
    Set<Permission> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);
    Set<Long>
        appPermissionIds =
        FluentIterable.from(createdAppPermissions).transform(permission -> permission.getId()).toSet();

    //create app master role
    Role appMasterRole = createRole(RoleUtils.buildAppMasterRoleName(appId), operator);

    rolePermissionService.createRoleWithPermissions(appMasterRole, appPermissionIds);
  }


  /**
   * 创建查看角色
   * @param appId
   * @param operator
   */
  private void createAppViewerRole(String appId, String operator) {

    Set<Permission> appPermissions = FluentIterable.from(Lists.newArrayList(PermissionType.BROWSE_CONFIG)).transform(permissionType -> createPermission(appId, permissionType, operator)).toSet();
    Set<Permission> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);
    Set<Long> appPermissionIds = FluentIterable.from(createdAppPermissions).transform(permission -> permission.getId()).toSet();

    // 创建查看角色
    Role appViewerRole = createRole(RoleUtils.buildViewerAppRoleName(appId), operator);

    rolePermissionService.createRoleWithPermissions(appViewerRole, appPermissionIds);
  }

  /**
   * 根据环境创建查看角色
   * @param appId
   * @param operator
   * @param env
   */
  private void createAppViewerEnvRole(String appId, String operator, String env) {

    Set<Permission> appPermissions = FluentIterable.from(Lists.newArrayList(PermissionType.BROWSE_CONFIG)).transform(permissionType -> createPermission(appId, permissionType, operator)).toSet();
    Set<Permission> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);
    Set<Long> appPermissionIds = FluentIterable.from(createdAppPermissions).transform(permission -> permission.getId()).toSet();

    // 创建查看角色
    Role appViewerRole = createRole(RoleUtils.buildViewerAppEnvRoleName(appId, env), operator);
    rolePermissionService.createRoleWithPermissions(appViewerRole, appPermissionIds);
  }

  private Permission createPermission(String targetId, String permissionType, String operator) {
    Permission permission = new Permission();
    permission.setPermissionType(permissionType);
    permission.setTargetId(targetId);
    permission.setDataChangeCreatedBy(operator);
    permission.setDataChangeLastModifiedBy(operator);
    return permission;
  }

  private Role createRole(String roleName, String operator) {
    Role role = new Role();
    role.setRoleName(roleName);
    role.setDataChangeCreatedBy(operator);
    role.setDataChangeLastModifiedBy(operator);
    return role;
  }

  private void createNamespaceRole(String appId, String namespaceName, String permissionType,
                                   String roleName, String operator) {

    Permission permission =
        createPermission(RoleUtils.buildNamespaceTargetId(appId, namespaceName), permissionType, operator);
    Permission createdPermission = rolePermissionService.createPermission(permission);

    Role role = createRole(roleName, operator);
    rolePermissionService
        .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
  }

  private void createNamespaceEnvRole(String appId, String namespaceName, String permissionType, String env,
                                      String roleName, String operator) {
    Permission permission =
        createPermission(RoleUtils.buildNamespaceTargetId(appId, namespaceName, env), permissionType, operator);
    Permission createdPermission = rolePermissionService.createPermission(permission);

    Role role = createRole(roleName, operator);
    rolePermissionService
        .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
  }
}
