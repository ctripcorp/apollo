package com.ctrip.framework.apollo.portal.constant;

public class RoleType {

  public static final String MASTER = "Master";

  public static final String MODIFY_NAMESPACE = "ModifyNamespace";

  public static final String RELEASE_NAMESPACE = "ReleaseNamespace";

  public static boolean isValidRoleType(String roleType) {
    return MASTER.equals(roleType) || MODIFY_NAMESPACE.equals(roleType) || RELEASE_NAMESPACE.equals(roleType);
  }

}
