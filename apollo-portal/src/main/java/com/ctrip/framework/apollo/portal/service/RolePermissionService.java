package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;

import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RolePermissionService {

  /**
   * Create role with permissions, note that role name should be unique
   */
  public Role createRoleWithPermissions(Role role, Set<Long> permissionIds);

  /**
   * Assign role to users
   *
   * @return the users assigned roles
   */
  public Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
                                       String operatorUserId);

  /**
   * Remove role from users
   */
  public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId);

  /**
   * Query users with role
   */
  public Set<UserInfo> queryUsersWithRole(String roleName);

  /**
   * Find role by role name, note that roleName should be unique
   */
  public Role findRoleByRoleName(String roleName);

  /**
   * Check whether user has the permission
   */
  public boolean userHasPermission(String userId, String permissionType, String targetId);

  public boolean isSuperAdmin(String userId);

  /**
   * Create permission, note that permissionType + targetId should be unique
   */
  public Permission createPermission(Permission permission);

  /**
   * Create permissions, note that permissionType + targetId should be unique
   */
  public Set<Permission> createPermissions(Set<Permission> permissions);

  /**
   * delete permissions when delete app.
   */
  public void deleteRolePermissionsByAppId(String appId, String operator);

  /**
   * delete permissions when delete app namespace.
   */
  public void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName, String operator);
}
