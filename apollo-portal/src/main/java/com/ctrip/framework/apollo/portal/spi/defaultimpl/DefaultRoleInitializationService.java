package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.BaseEntity;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.service.SystemRoleManagerService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认角色初始化 Service.
 *
 * @author timothy
 * @data 2017/4/26.
 */
public class DefaultRoleInitializationService implements RoleInitializationService {

  @Autowired
  private RolePermissionService rolePermissionService;
  @Autowired
  private PortalConfig portalConfig;
  @Autowired
  private PermissionRepository permissionRepository;

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initAppRoles(App app) {
    String appId = app.getAppId();

    String appMasterRoleName = RoleUtils.buildAppMasterRoleName(appId);

    // 之前创建过
    if (rolePermissionService.findRoleByRoleName(appMasterRoleName) != null) {
      return;
    }
    String operator = app.getDataChangeCreatedBy();
    //创建应用权限
    createAppMasterRole(appId, operator);
    //创建应用管理员权限
    createManageAppMasterRole(appId, operator);

    // 分配管理员角色给用户
    rolePermissionService
        .assignRoleToUsers(RoleUtils.buildAppMasterRoleName(appId),
            Sets.newHashSet(app.getOwnerName()),
            operator);

    // 初始化名称空间和名称空间环境角色列表
    initNamespaceRoles(appId, ConfigConsts.NAMESPACE_APPLICATION, operator);
    initNamespaceEnvRoles(appId, ConfigConsts.NAMESPACE_APPLICATION, operator);

    // 分配修改，发布名称空间的角色给用户
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts
        .NAMESPACE_APPLICATION, RoleType.MODIFY_NAMESPACE), Sets.newHashSet(operator), operator);
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts
        .NAMESPACE_APPLICATION, RoleType.RELEASE_NAMESPACE), Sets.newHashSet(operator), operator);

  }

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initNamespaceRoles(String appId, String namespaceName, String operator) {

    //创建名称空间修改角色
    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName) == null) {
      createNamespaceRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE,
          modifyNamespaceRoleName, operator);
    }

    //创建名称空间发布角色
    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName) == null) {
      createNamespaceRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE,
          releaseNamespaceRoleName, operator);
    }
  }

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initNamespaceEnvRoles(String appId, String namespaceName, String operator) {
    List<Env> portalEnvs = portalConfig.portalSupportedEnvs();
    //初始化名称空间指定的环境角色
    for (Env env : portalEnvs) {
      initNamespaceSpecificEnvRoles(appId, namespaceName, env.toString(), operator);
    }
  }

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initNamespaceSpecificEnvRoles(String appId, String namespaceName, String env,
      String operator) {

    // 创建修改名称空间环境的角色
    String modifyNamespaceEnvRoleName = RoleUtils
        .buildModifyNamespaceRoleName(appId, namespaceName, env);
    if (rolePermissionService.findRoleByRoleName(modifyNamespaceEnvRoleName) == null) {
      createNamespaceEnvRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE, env,
          modifyNamespaceEnvRoleName, operator);
    }

    // 创建发布名称空间环境的角色
    String releaseNamespaceEnvRoleName = RoleUtils
        .buildReleaseNamespaceRoleName(appId, namespaceName, env);
    if (rolePermissionService.findRoleByRoleName(releaseNamespaceEnvRoleName) == null) {
      createNamespaceEnvRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE, env,
          releaseNamespaceEnvRoleName, operator);
    }
  }

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initCreateAppRole() {
    if (rolePermissionService
        .findRoleByRoleName(SystemRoleManagerService.CREATE_APPLICATION_ROLE_NAME) != null) {
      return;
    }
    Permission createAppPermission = permissionRepository
        .findTopByPermissionTypeAndTargetId(PermissionType.CREATE_APPLICATION,
            SystemRoleManagerService.SYSTEM_PERMISSION_TARGET_ID);
    if (createAppPermission == null) {
      // 构建初始化的应用权限
      createAppPermission = createPermission(SystemRoleManagerService.SYSTEM_PERMISSION_TARGET_ID,
          PermissionType.CREATE_APPLICATION, "apollo");
      rolePermissionService.createPermission(createAppPermission);
    }
    //  构建应用角色初始化
    Role createAppRole = createRole(SystemRoleManagerService.CREATE_APPLICATION_ROLE_NAME,
        "apollo");

    // 创建角色和其具有的权限
    rolePermissionService
        .createRoleWithPermissions(createAppRole, Sets.newHashSet(createAppPermission.getId()));
  }

  /**
   * 创建应用管理员权限和角色
   *
   * @param appId    应用id
   * @param operator 操作者
   */
   @Transactional(rollbackFor = Exception.class)
  public void createManageAppMasterRole(String appId, String operator) {
    //创建应用管理员权限
    Permission permission = createPermission(appId, PermissionType.MANAGE_APP_MASTER, operator);
    rolePermissionService.createPermission(permission);

    //创建角色权限
    Role role = createRole(RoleUtils.buildAppRoleName(appId, PermissionType.MANAGE_APP_MASTER),
        operator);
    Set<Long> permissionIds = new HashSet<>();
    permissionIds.add(permission.getId());
    rolePermissionService.createRoleWithPermissions(role, permissionIds);
  }

  // 修改历史数据

  @Override
   @Transactional(rollbackFor = Exception.class)
  public void initManageAppMasterRole(String appId, String operator) {

    String manageAppMasterRoleName = RoleUtils
        .buildAppRoleName(appId, PermissionType.MANAGE_APP_MASTER);
    //用户存在不创建
    if (rolePermissionService.findRoleByRoleName(manageAppMasterRoleName) != null) {
      return;
    }
    //创建应用管理员
    synchronized (DefaultRoleInitializationService.class) {
      createManageAppMasterRole(appId, operator);
    }
  }

  /**
   * 创建应用管理员角色
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  private void createAppMasterRole(String appId, String operator) {
    // 构建的应用权限列表
    Set<Permission> appPermissions = Stream.of(PermissionType.CREATE_CLUSTER, PermissionType
        .CREATE_NAMESPACE, PermissionType.ASSIGN_ROLE).map(permissionType -> createPermission(appId,
        permissionType, operator)).collect(Collectors.toSet());

    // 创建权限
    Set<Permission> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);

    Set<Long> appPermissionIds = createdAppPermissions.stream().map(BaseEntity::getId)
        .collect(Collectors.toSet());

    // 构建应用管理员角色
    Role appMasterRole = createRole(RoleUtils.buildAppMasterRoleName(appId), operator);
    // 创建角色权限
    rolePermissionService.createRoleWithPermissions(appMasterRole, appPermissionIds);
  }

  /**
   * 构建权限对象
   *
   * @param targetId       权限对象类型
   * @param permissionType 权限类型
   * @param operator       操作者
   * @return 构建的权限对象
   */
  private Permission createPermission(String targetId, String permissionType, String operator) {
    Permission permission = new Permission();
    permission.setPermissionType(permissionType);
    permission.setTargetId(targetId);
    permission.setDataChangeCreatedBy(operator);
    permission.setDataChangeLastModifiedBy(operator);
    return permission;
  }

  /**
   * 构建角色对象
   *
   * @param roleName 角色名称
   * @param operator 操作者
   * @return 构建的权限对象
   */
  private Role createRole(String roleName, String operator) {
    Role role = new Role();
    role.setRoleName(roleName);
    role.setDataChangeCreatedBy(operator);
    role.setDataChangeLastModifiedBy(operator);
    return role;
  }

  /**
   * 创建名称空间角色
   *
   * @param appId          应用id
   * @param namespaceName  名称空间名称
   * @param permissionType 权限类型
   * @param roleName       角色名称
   * @param operator       操作者
   */
  private void createNamespaceRole(String appId, String namespaceName, String permissionType,
      String roleName, String operator) {
    // 创建权限
    Permission permission = createPermission(RoleUtils.buildNamespaceTargetId(appId, namespaceName),
        permissionType, operator);
    Permission createdPermission = rolePermissionService.createPermission(permission);

    // 创建角色和其权限
    Role role = createRole(roleName, operator);
    rolePermissionService
        .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
  }

  /**
   * 创建名称空间环境的角色
   *
   * @param appId          应用id
   * @param namespaceName  名称空间名称
   * @param permissionType 权限类型
   * @param env            环境
   * @param roleName       角色名称
   * @param operator       操作者
   */
  private void createNamespaceEnvRole(String appId, String namespaceName, String permissionType,
      String env, String roleName, String operator) {
    // 创建权限
    Permission permission = createPermission(RoleUtils.buildNamespaceTargetId(appId, namespaceName,
        env), permissionType, operator);
    Permission createdPermission = rolePermissionService.createPermission(permission);

    Role role = createRole(roleName, operator);
    // 创建角色和其权限
    rolePermissionService
        .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
  }
}
