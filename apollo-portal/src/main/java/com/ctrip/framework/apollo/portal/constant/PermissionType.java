package com.ctrip.framework.apollo.portal.constant;

public interface PermissionType {

  /**
   * APP level permission
   */

  String CREATE_NAMESPACE = "CreateNamespace";

  String CREATE_CLUSTER = "CreateCluster";

  /**
   * 分配用户权限的权限
   */
  String ASSIGN_ROLE = "AssignRole";

  /**
   * namespace level permission
   */

  String MODIFY_NAMESPACE = "ModifyNamespace";

  String RELEASE_NAMESPACE = "ReleaseNamespace";

  /**
   * 查看配置的权限
   */
  String BROWSE_CONFIG = "BorwseConfig";

}
