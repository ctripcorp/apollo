package com.ctrip.framework.apollo.portal.constant;

/**
 * 角色类型
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class RoleType {

  /**
   * 管理员（所有权限<主宰>）角色
   */
  public static final String MASTER = "Master";
  /**
   * 修改名称空间角色
   */
  public static final String MODIFY_NAMESPACE = "ModifyNamespace";
  /**
   * 发布名称空间角色
   */
  public static final String RELEASE_NAMESPACE = "ReleaseNamespace";

  /**
   * 验证是否为角色类型
   *
   * @param roleType 角色类型字符串
   * @return true，是角色类型，否则，false
   */
  public static boolean isValidRoleType(String roleType) {
    return MASTER.equals(roleType) || MODIFY_NAMESPACE.equals(roleType) || RELEASE_NAMESPACE
        .equals(roleType);
  }
}