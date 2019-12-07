package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Set;

/**
 * Property source wrapper for Config
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySource extends EnumerablePropertySource<Config> {
  private static final String[] EMPTY_ARRAY = new String[0];

  ConfigPropertySource(String name, Config source) {
    super(name, source);
  }

  @Override
  public String[] getPropertyNames() {
    Set<String> propertyNames = this.source.getPropertyNames();
    if (propertyNames.isEmpty()) {
      return EMPTY_ARRAY;
    }
    return propertyNames.toArray(new String[propertyNames.size()]);
  }

  @Override
  public Object getProperty(String name) {
    return this.source.getProperty(name, null);
  }

  public void addChangeListener(ConfigChangeListener listener) {
    this.source.addChangeListener(listener);
  }
}
