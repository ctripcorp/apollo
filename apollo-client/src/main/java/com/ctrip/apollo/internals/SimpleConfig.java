package com.ctrip.apollo.internals;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import com.ctrip.apollo.model.ConfigChange;
import com.ctrip.apollo.model.ConfigChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class SimpleConfig extends AbstractConfig implements RepositoryChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(SimpleConfig.class);
  private final String m_namespace;
  private final ConfigRepository m_configRepository;
  private volatile Properties m_configProperties;

  public SimpleConfig(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_configRepository = configRepository;
    this.initialize();
  }

  private void initialize() {
    try {
      m_configProperties = m_configRepository.getConfig();
      m_configRepository.addChangeListener(this);
    } catch (Throwable ex) {
      String message = String.format("Init Apollo Simple Config failed - namespace: %s",
          m_namespace);
      logger.error(message, ex);
      throw new RuntimeException(message, ex);
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    return this.m_configProperties.getProperty(key, defaultValue);
  }

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    if (newProperties.equals(m_configProperties)) {
      return;
    }
    Properties newConfigProperties = new Properties();
    newConfigProperties.putAll(newProperties);

    List<ConfigChange> changes = calcPropertyChanges(m_configProperties, newConfigProperties);
    Map<String, ConfigChange> changeMap = Maps.uniqueIndex(changes,
        new Function<ConfigChange, String>() {
          @Override
          public String apply(ConfigChange input) {
            return input.getPropertyName();
          }
        });

    m_configProperties = newConfigProperties;

    this.fireConfigChange(new ConfigChangeEvent(m_namespace, changeMap));
  }
}
