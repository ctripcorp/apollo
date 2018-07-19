package com.ctrip.framework.apollo.portal.util;

import com.google.common.base.Joiner;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.constant.RoleType;

public class RoleUtils {

  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

  public static String buildAppMasterRoleName(String appId) {
    return STRING_JOINER.join(RoleType.MASTER, appId);
  }

  public static String buildAppRoleName(String appId, String roleType) {
    return STRING_JOINER.join(roleType, appId);
  }

  public static String buildModifyNamespaceRoleName(String appId, String namespaceName, String env) {
    if (null != env && !"".equals(env))
      return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, namespaceName, env);
    return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, namespaceName);
  }

  public static String buildModifyDefaultNamespaceRoleName(String appId) {
    return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, ConfigConsts.NAMESPACE_APPLICATION);
  }

  public static String buildReleaseNamespaceRoleName(String appId, String namespaceName, String env) {
    if (null != env && !"".equals(env))
      return STRING_JOINER.join(RoleType.RELEASE_NAMESPACE, appId, namespaceName, env);
    return STRING_JOINER.join(RoleType.RELEASE_NAMESPACE, appId, namespaceName);
  }

  public static String buildNamespaceRoleName(String appId, String namespaceName, String roleType, String env) {
    if (null != env && !"".equals(env))
      return STRING_JOINER.join(roleType, appId, namespaceName, env);
    return STRING_JOINER.join(roleType, appId, namespaceName);
  }

  public static String buildReleaseDefaultNamespaceRoleName(String appId) {
    return STRING_JOINER.join(RoleType.RELEASE_NAMESPACE, appId, ConfigConsts.NAMESPACE_APPLICATION);
  }

  public static String buildNamespaceTargetId(String appId, String namespaceName, String env) {
    if (null != env && !"".equals(env))
      return STRING_JOINER.join(appId, namespaceName, env);
    return STRING_JOINER.join(appId, namespaceName);
  }

  public static String buildDefaultNamespaceTargetId(String appId) {
    return STRING_JOINER.join(appId, ConfigConsts.NAMESPACE_APPLICATION);
  }


}
