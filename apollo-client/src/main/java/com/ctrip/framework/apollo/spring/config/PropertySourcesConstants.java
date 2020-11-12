package com.ctrip.framework.apollo.spring.config;

/**
 * 属性源常量
 */
public interface PropertySourcesConstants {

  /**
   * apollo属性源名称
   */
  String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";
  /**
   * apollo引导属性源名称
   */
  String APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME = "ApolloBootstrapPropertySources";
  /**
   * 启动阶段是否注入配置
   */
  String APOLLO_BOOTSTRAP_ENABLED = "apollo.bootstrap.enabled";
  /**
   * 是否将apollo的加载放到日志系统的加载之前，这样就可以通过apollo来管理日志相关的配置，如日志级别的设置等，如果为true，则会在这个阶段apollo不会有日志输出
   */
  String APOLLO_BOOTSTRAP_EAGER_LOAD_ENABLED = "apollo.bootstrap.eagerLoad.enabled";
  /**
   * apollo 使用配置的命名空间，多个以逗号分隔
   */
  String APOLLO_BOOTSTRAP_NAMESPACES = "apollo.bootstrap.namespaces";
}
