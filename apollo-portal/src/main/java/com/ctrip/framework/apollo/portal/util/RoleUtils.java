package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.Iterator;

/**
 * 角色工具类
 */
public class RoleUtils {

  private RoleUtils() {
  }

  /**
   * 字符串拼接器（跳过Null值）
   */
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
      .skipNulls();
  /**
   * 字符串分割器(忽略空字符串、空格)
   */
  private static final Splitter STRING_SPLITTER = Splitter
      .on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
      .omitEmptyStrings().trimResults();

  /**
   * 通过应用id构建应用的Master角色名称
   *
   * @param appId 应用id
   * @return 构建应用的Master角色名称
   */
  public static String buildAppMasterRoleName(String appId) {
    return STRING_JOINER.join(RoleType.MASTER, appId);
  }

  /**
   * 从Master角色名称提取应用id
   *
   * @param masterRoleName Master角色名称
   * @return 提取的应用id
   */
  public static String extractAppIdFromMasterRoleName(String masterRoleName) {
    Iterator<String> parts = STRING_SPLITTER.split(masterRoleName).iterator();

    //  跳过角色类型
    if (parts.hasNext() && parts.next().equals(RoleType.MASTER) && parts.hasNext()) {
      return parts.next();
    }

    return null;
  }

  /**
   * 从角色名称提取应用id
   *
   * @param roleName 角色名称
   * @return 提取的应用id
   */
  public static String extractAppIdFromRoleName(String roleName) {
    Iterator<String> parts = STRING_SPLITTER.split(roleName).iterator();
    if (parts.hasNext()) {
      String roleType = parts.next();
      if (RoleType.isValidRoleType(roleType) && parts.hasNext()) {
        return parts.next();
      }
    }
    return null;
  }

  /**
   * 通过应用id、角色类型构建应用的角色名称
   *
   * @param appId    应用id
   * @param roleType 角色类型
   * @return 构建应用的角色名称
   */
  public static String buildAppRoleName(String appId, String roleType) {
    return STRING_JOINER.join(roleType, appId);
  }

  /**
   * 通过应用id、名称空间名称构建修改名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 构建修改名称空间的角色名称
   */
  public static String buildModifyNamespaceRoleName(String appId, String namespaceName) {
    return buildModifyNamespaceRoleName(appId, namespaceName, null);
  }

  /**
   * 通过应用id、名称空间名称、环境构建修改名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return 构建修改名称空间的角色名称
   */
  public static String buildModifyNamespaceRoleName(String appId, String namespaceName,
      String env) {
    return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, namespaceName, env);
  }

  /**
   * 通过应用id构建修改名称空间的角色名称
   *
   * @param appId 应用id
   * @return 构建修改名称空间的角色名称
   */
  public static String buildModifyDefaultNamespaceRoleName(String appId) {
    return STRING_JOINER.join(RoleType.MODIFY_NAMESPACE, appId, ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * 通过应用id、名称空间名称、构建发布名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 构建发布名称空间的角色名称
   */
  public static String buildReleaseNamespaceRoleName(String appId, String namespaceName) {
    return buildReleaseNamespaceRoleName(appId, namespaceName, null);
  }

  /**
   * 通过应用id、名称空间名称、环境构建发布名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return 构建发布名称空间的角色名称
   */
  public static String buildReleaseNamespaceRoleName(String appId, String namespaceName,
      String env) {
    return STRING_JOINER.join(RoleType.RELEASE_NAMESPACE, appId, namespaceName, env);
  }

  /**
   * 通过应用id、名称空间名称、角色类型构建名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @return 构建名称空间的角色名称
   */
  public static String buildNamespaceRoleName(String appId, String namespaceName, String roleType) {
    return buildNamespaceRoleName(appId, namespaceName, roleType, null);
  }

  /**
   * 通过应用id、名称空间名称、角色类型、环境构建名称空间的角色名称
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @param env           环境
   * @return 构建名称空间的角色名称
   */
  public static String buildNamespaceRoleName(String appId, String namespaceName, String roleType,
      String env) {
    return STRING_JOINER.join(roleType, appId, namespaceName, env);
  }

  /**
   * 通过应用id构建发布默认名称空间的角色名称
   *
   * @param appId 应用id
   * @return 构建的发布默认名称空间的角色名称
   */
  public static String buildReleaseDefaultNamespaceRoleName(String appId) {
    return STRING_JOINER
        .join(RoleType.RELEASE_NAMESPACE, appId, ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * 通过应用id、名称空间名称构建名称空间的权限对象类型
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 构建的权限对象类型
   */
  public static String buildNamespaceTargetId(String appId, String namespaceName) {
    return buildNamespaceTargetId(appId, namespaceName, null);
  }

  /**
   * 通过应用id、名称空间名称、环境构建名称空间的权限对象类型
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return 构建的名称空间的权限对象类型
   */
  public static String buildNamespaceTargetId(String appId, String namespaceName, String env) {
    return STRING_JOINER.join(appId, namespaceName, env);
  }

  /**
   * 通过应用id构建默认名称空间的权限对象类型
   *
   * @param appId 应用id
   * @return 构建的默认名称空间的权限对象类型
   */
  public static String buildDefaultNamespaceTargetId(String appId) {
    return STRING_JOINER.join(appId, ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * 通过权限类型、权限对象类型构建创建应用的角色名称
   *
   * @param permissionType     权限类型
   * @param permissionTargetId 权限对象类型
   * @return 构建创建应用的角色名称
   */
  public static String buildCreateApplicationRoleName(String permissionType,
      String permissionTargetId) {
    return STRING_JOINER.join(permissionType, permissionTargetId);
  }
}
