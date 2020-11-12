package com.ctrip.framework.apollo.core;

/**
 * 配置常量.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigConsts {

  /**
   * 默认的名称空间
   */
  String NAMESPACE_APPLICATION = "application";
  /**
   * 集群名称默认值
   */
  String CLUSTER_NAME_DEFAULT = "default";
  /**
   * 集群名称拼接符
   */
  String CLUSTER_NAMESPACE_SEPARATOR = "+";
  /**
   * 默认的集群key
   */
  String APOLLO_CLUSTER_KEY = "apollo.cluster";
  /**
   * 默认的元服务地址key
   */
  String APOLLO_META_KEY = "apollo.meta";
  /**
   * 配置文件内容key
   */
  String CONFIG_FILE_CONTENT_KEY = "content";
  /**
   * 无AppId占位符
   */
  String NO_APPID_PLACEHOLDER = "ApolloNoAppIdPlaceHolder";
  /**
   * 初始化通知id占位符
   */
  long NOTIFICATION_ID_PLACEHOLDER = -1;
}
