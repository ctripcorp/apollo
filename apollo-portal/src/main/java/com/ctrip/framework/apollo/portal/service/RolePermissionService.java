package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import java.util.List;
import java.util.Set;

/**
 * 角色权限 Service.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RolePermissionService {

  /**
   * 创建角色与具有其权限，注意，角色名称应该是唯一的
   *
   * @param role          角色
   * @param permissionIds 权限
   * @return 创建的角色对象
   */
  Role createRoleWithPermissions(Role role, Set<Long> permissionIds);

  /**
   * 分配用户角色
   *
   * @param roleName       角色名称
   * @param userIds        用户id列表
   * @param operatorUserId 操作人id
   * @return 用户id集
   */
  Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
      String operatorUserId);

  /**
   * 删除指定用户id列表和角色id的用户角色信息
   *
   * @param roleName       角色名称
   * @param userIds        用户id列表
   * @param operatorUserId 操作人id
   */
  void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId);

  /**
   * 根据角色名查询用户信息
   *
   * @param roleName 角色名
   * @return 指定角色名下的用户信息
   */
  Set<UserInfo> queryUsersWithRole(String roleName);

  /**
   * 根据角色名获取角色信息
   *
   * @param roleName 角色名
   * @return 指定角色名的角色信息
   */
  Role findRoleByRoleName(String roleName);

  /**
   * 检查指定用户下的角色是否具有指定权限
   *
   * @param userId         用户id
   * @param permissionType 权限类型
   * @param targetId       权限对象类型
   * @return true 有权限，否则，false
   */
  boolean userHasPermission(String userId, String permissionType, String targetId);

  /**
   * 查找指定用户的角色列表
   *
   * @param userId 用户id
   * @return 角色列表
   */
  List<Role> findUserRoles(String userId);

  /**
   * 是否为超级管理员
   *
   * @param userId 用户身份标识(用户名)
   * @return 如果为超级管理员, true, 否则，false
   */
  boolean isSuperAdmin(String userId);

  /**
   * 创建权限， 请注意，permissionType+targetId应该是唯一的
   *
   * @param permission 待创建的权限对象
   * @return 已创建的权限对象
   */
  Permission createPermission(Permission permission);

  /**
   * 批量创建权限， 请注意，permissionType+targetId应该是唯一的
   *
   * @param permissions 创建的权限对象列表
   * @return 已创建的权限对象
   */
  Set<Permission> createPermissions(Set<Permission> permissions);

  /**
   * 通过appId删除权限
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  void deleteRolePermissionsByAppId(String appId, String operator);

  /**
   * 通过appId和名称空间名称删除权限 .
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   */
  void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName,
      String operator);
}
