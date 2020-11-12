package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * ConfigPropertySource 工厂
 */
public class ConfigPropertySourceFactory {

  /**
   * ConfigPropertySource 数组
   */
  private final List<ConfigPropertySource> configPropertySources = Lists.newLinkedList();

  /**
   * 创建 ConfigPropertySource 对象
   *
   * @param name   属性名称
   * @param source 配置
   * @return 创建的 ConfigPropertySource 对象
   */
  public ConfigPropertySource getConfigPropertySource(String name, Config source) {
    // 创建 ConfigPropertySource 对象
    ConfigPropertySource configPropertySource = new ConfigPropertySource(name, source);
    // 添加到数组中
    configPropertySources.add(configPropertySource);
    return configPropertySource;
  }

  /**
   * 获取所有的ConfigPropertySource列表信息
   *
   * @return 所有的ConfigPropertySource列表信息
   */
  public List<ConfigPropertySource> getAllConfigPropertySources() {
    return Lists.newLinkedList(configPropertySources);
  }
}
