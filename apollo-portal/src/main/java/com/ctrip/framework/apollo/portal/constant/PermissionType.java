package com.ctrip.framework.apollo.portal.constant;

/**
 * 权限类型.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PermissionType {

  // system level permission
  /**
   * 创建应用权限
   */
  String CREATE_APPLICATION = "CreateApplication";
  /**
   * 管理应用的责任人权限
   */
  String MANAGE_APP_MASTER = "ManageAppMaster";

  // APP level permission
  /**
   * 创建名称空间权限
   */
  String CREATE_NAMESPACE = "CreateNamespace";
  /**
   * 创建集群权限
   */
  String CREATE_CLUSTER = "CreateCluster";

  /**
   * 分配用户权限的权限
   */
  String ASSIGN_ROLE = "AssignRole";

  // namespace level permission
  /**
   * 修改名称空间权限
   */
  String MODIFY_NAMESPACE = "ModifyNamespace";
  /**
   * 发布名称空间权限
   */
  String RELEASE_NAMESPACE = "ReleaseNamespace";
}