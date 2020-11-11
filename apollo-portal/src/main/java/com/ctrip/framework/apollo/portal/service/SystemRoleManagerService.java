package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统角色管理 Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class SystemRoleManagerService {

  public static final Logger logger = LoggerFactory.getLogger(SystemRoleManagerService.class);
  /**
   * 系统权限权限对象类型
   */
  public static final String SYSTEM_PERMISSION_TARGET_ID = "SystemRole";
  /**
   * 创建应用的角色名称
   */
  public static final String CREATE_APPLICATION_ROLE_NAME = RoleUtils
      .buildCreateApplicationRoleName(PermissionType.CREATE_APPLICATION,
          SYSTEM_PERMISSION_TARGET_ID);
  /**
   * 限制只有超级管理员和拥有创建应用权限的帐号可以创建项目的Key
   */
  public static final String CREATE_APPLICATION_LIMIT_SWITCH_KEY = "role.create-application.enabled";
  /**
   * 限制只有超级管理员和拥有管理员分配权限的帐号可以修改项目管理员的Key
   */
  public static final String MANAGE_APP_MASTER_LIMIT_SWITCH_KEY = "role.manage-app-master.enabled";

  private final RolePermissionService rolePermissionService;

  private final PortalConfig portalConfig;

  private final RoleInitializationService roleInitializationService;

  @Autowired
  public SystemRoleManagerService(final RolePermissionService rolePermissionService,
      final PortalConfig portalConfig,
      final RoleInitializationService roleInitializationService) {
    this.rolePermissionService = rolePermissionService;
    this.portalConfig = portalConfig;
    this.roleInitializationService = roleInitializationService;
  }

  @PostConstruct
  private void init() {
    roleInitializationService.initCreateAppRole();
  }

  /**
   * 是否限制只有超级管理员和拥有创建应用权限的帐号可以创建项目
   *
   * @return true，限制，false，不限制
   */
  private boolean isCreateApplicationPermissionEnabled() {
    return portalConfig.isCreateApplicationPermissionEnabled();
  }

  /**
   * 是否限制只有超级管理员和拥有管理员分配权限的帐号可以修改项目
   *
   * @return true，限制，false，不限制
   */
  public boolean isManageAppMasterPermissionEnabled() {
    return portalConfig.isManageAppMasterPermissionEnabled();
  }

  /**
   * 检查指定用户下的角色是否具有创建应用的系统权限
   *
   * @param userId 用户id
   * @return true，具有指定的权限 ，否则，false
   */
  public boolean hasCreateApplicationPermission(String userId) {
    // 不限制的情况
    if (!isCreateApplicationPermissionEnabled()) {
      return true;
    }

    return rolePermissionService
        .userHasPermission(userId, PermissionType.CREATE_APPLICATION, SYSTEM_PERMISSION_TARGET_ID);
  }

  /**
   * 检查指定用户下的角色是否具有指定权限
   *
   * @param userId 指定的用户id
   * @param appId  指定的应用id
   * @return true, 具有指定的权限，否则，false
   */
  public boolean hasManageAppMasterPermission(String userId, String appId) {
    if (!isManageAppMasterPermissionEnabled()) {
      return true;
    }
    return rolePermissionService.userHasPermission(userId, PermissionType.MANAGE_APP_MASTER, appId);
  }
}
