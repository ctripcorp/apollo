package com.ctrip.framework.apollo.common.config;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public abstract class RefreshablePropertySource extends MapPropertySource {


  public RefreshablePropertySource(String name, Map<String, Object> source) {
    super(name, source);
  }

  @Override
  public Object getProperty(String name) {
    return this.source.get(name);
  }

  /**
   * refresh property
   */
  protected abstract void refresh();

}
