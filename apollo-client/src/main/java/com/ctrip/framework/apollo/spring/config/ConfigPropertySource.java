package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;

import org.springframework.core.env.PropertySource;

/**
 * Property source wrapper for Config
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySource extends PropertySource<Config> {
  public ConfigPropertySource(String name, Config source) {
    super(name, source);
  }

  @Override
  public Object getProperty(String name) {
    return this.source.getProperty(name, null);
  }
}
