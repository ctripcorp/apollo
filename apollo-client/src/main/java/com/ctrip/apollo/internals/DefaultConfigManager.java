package com.ctrip.apollo.internals;

import com.google.common.collect.Maps;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.spi.ConfigFactory;
import com.ctrip.apollo.spi.ConfigFactoryManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigManager.class, value = "default")
public class DefaultConfigManager implements ConfigManager {
  @Inject
  private ConfigFactoryManager m_factoryManager;

  private Map<String, Config> m_configs = Maps.newConcurrentMap();

  @Override
  public Config getConfig(String namespace) {
    Config config = m_configs.get(namespace);

    if (config == null) {
      synchronized (this) {
        config = m_configs.get(namespace);

        if (config == null) {
          ConfigFactory factory = m_factoryManager.getFactory(namespace);

          config = factory.create(namespace);
          m_configs.put(namespace, config);
        }
      }
    }

    return config;
  }
}
