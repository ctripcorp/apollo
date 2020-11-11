package com.ctrip.framework.apollo.portal.constant;

/**
 * 事件跟踪器类型
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface TracerEventType {

  /**
   * 发布名称空间事件
   */
  String RELEASE_NAMESPACE = "Namespace.Release";
  /**
   * 修改名称空间文本事件
   */
  String MODIFY_NAMESPACE_BY_TEXT = "Namespace.Modify.Text";
  /**
   * 修改名称空间事件
   */
  String MODIFY_NAMESPACE = "Namespace.Modify";
  /**
   * 同步名称空间事件
   */
  String SYNC_NAMESPACE = "Namespace.Sync";
  /**
   * 创建app事件
   */
  String CREATE_APP = "App.Create";
  /**
   * 创建集群事件
   */
  String CREATE_CLUSTER = "Cluster.Create";
  /**
   * 创建授权key事件
   */
  String CREATE_ACCESS_KEY = "AccessKey.Create";
  /**
   * 创建名称空间事件
   */
  String CREATE_NAMESPACE = "Namespace.Create";
  /**
   * api重试事件
   */
  String API_RETRY = "API.Retry";
  /**
   * 用户访问事件
   */
  String USER_ACCESS = "User.Access";
  /**
   * 创建灰度发布事件
   */
  String CREATE_GRAY_RELEASE = "GrayRelease.Create";
  /**
   * 删除灰度发布事件
   */
  String DELETE_GRAY_RELEASE = "GrayRelease.Delete";
  /**
   * 合并灰度发布事件
   */
  String MERGE_GRAY_RELEASE = "GrayRelease.Merge";
  /**
   * 更新灰度发布规则事件
   */
  String UPDATE_GRAY_RELEASE_RULE = "GrayReleaseRule.Update";
}