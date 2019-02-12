package com.ctrip.framework.apollo.portal.constant;

/**
 * 权限类型
 */
public interface PermissionType {


  /**
   * 应用级别的权限
   */
  String CREATE_NAMESPACE = "CreateNamespace";
  String CREATE_CLUSTER = "CreateCluster";

  /**
   * 分配用户权限的权限
   */
  String ASSIGN_ROLE = "AssignRole";


  /**
   * namespace级别的权限
   */
  String MODIFY_NAMESPACE = "ModifyNamespace";
  String RELEASE_NAMESPACE = "ReleaseNamespace";

  /**
   * 查看配置的权限
   */
  String BROWSE_CONFIG = "BrowseConfig";

}
