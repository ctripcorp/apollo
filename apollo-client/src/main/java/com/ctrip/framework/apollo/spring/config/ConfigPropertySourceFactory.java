package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;
import com.google.common.collect.Lists;

import java.util.List;

public class ConfigPropertySourceFactory {

  private final List<ConfigPropertySource> configPropertySources = Lists.newLinkedList();

  public ConfigPropertySource getConfigPropertySource(String name, Config source) {
    ConfigPropertySource configPropertySource = new ConfigPropertySource(name, source);

    configPropertySources.add(configPropertySource);

    return configPropertySource;
  }

  public List<ConfigPropertySource> getAllConfigPropertySources() {
    return Lists.newLinkedList(configPropertySources);
  }
}
