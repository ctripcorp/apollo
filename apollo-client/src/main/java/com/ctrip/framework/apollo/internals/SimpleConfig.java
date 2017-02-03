package com.ctrip.framework.apollo.internals;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class SimpleConfig extends AbstractConfig implements RepositoryChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(SimpleConfig.class);
  private final String m_namespace;
  private final ConfigRepository m_configRepository;
  private volatile Properties m_configProperties;

  /**
   * Constructor.
   *
   * @param namespace        the namespace for this config instance
   * @param configRepository the config repository for this config instance
   */
  public SimpleConfig(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_configRepository = configRepository;
    this.initialize();
  }

  private void initialize() {
    try {
      m_configProperties = m_configRepository.getConfig();
    } catch (Throwable ex) {
      Tracer.logError(ex);
      logger.warn("Init Apollo Simple Config failed - namespace: {}, reason: {}", m_namespace,
          ExceptionUtil.getDetailMessage(ex));
    } finally {
      //register the change listener no matter config repository is working or not
      //so that whenever config repository is recovered, config could get changed
      m_configRepository.addChangeListener(this);
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    if (m_configProperties == null) {
      logger.warn("Could not load config from Apollo, always return default value!");
      return defaultValue;
    }
    return this.m_configProperties.getProperty(key, defaultValue);
  }

  @Override
  public Set<String> getPropertyNames() {
    if (m_configProperties == null) {
      return Collections.emptySet();
    }

    return m_configProperties.stringPropertyNames();
  }

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    if (newProperties.equals(m_configProperties)) {
      return;
    }
    Properties newConfigProperties = new Properties();
    newConfigProperties.putAll(newProperties);

    List<ConfigChange>
        changes =
        calcPropertyChanges(namespace, m_configProperties, newConfigProperties);
    Map<String, ConfigChange> changeMap = Maps.uniqueIndex(changes,
        new Function<ConfigChange, String>() {
          @Override
          public String apply(ConfigChange input) {
            return input.getPropertyName();
          }
        });

    m_configProperties = newConfigProperties;
    clearConfigCache();

    this.fireConfigChange(new ConfigChangeEvent(m_namespace, changeMap));

    Tracer.logEvent("Apollo.Client.ConfigChanges", m_namespace);
  }
}
