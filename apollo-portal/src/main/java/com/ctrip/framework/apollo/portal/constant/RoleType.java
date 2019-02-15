package com.ctrip.framework.apollo.portal.constant;

public class RoleType {

  /**
   * 超级管理员角色
   */
  public static final String MASTER = "Master";

  /**
   * 修改Namespace角色
   */
  public static final String MODIFY_NAMESPACE = "ModifyNamespace";

  /**
   * 发布配置角色
   */
  public static final String RELEASE_NAMESPACE = "ReleaseNamespace";

  /**
   * 配置查看角色
   */
  public static final String VIEW = "View";

  public static boolean isValidRoleType(String roleType) {
    return MASTER.equals(roleType) || MODIFY_NAMESPACE.equals(roleType) || RELEASE_NAMESPACE.equals(roleType) || VIEW.equals(roleType);
  }

}
