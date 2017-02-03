package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

/**
 * Entry point for client config use
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {
  private static final ConfigService s_instance = new ConfigService();

  private PlexusContainer m_container;
  private volatile ConfigManager m_configManager;
  private volatile ConfigRegistry m_configRegistry;

  private ConfigService() {
    m_container = ContainerLoader.getDefaultContainer();
  }

  private ConfigManager getManager() {
    if (m_configManager == null) {
      synchronized (this) {
        if (m_configManager == null) {
          try {
            m_configManager = m_container.lookup(ConfigManager.class);
          } catch (ComponentLookupException ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to load ConfigManager!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }

    return m_configManager;
  }

  private ConfigRegistry getRegistry() {
    if (m_configRegistry == null) {
      synchronized (this) {
        if (m_configRegistry == null) {
          try {
            m_configRegistry = m_container.lookup(ConfigRegistry.class);
          } catch (ComponentLookupException ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to load ConfigRegistry!", ex);
            Tracer.logError(exception);
            throw exception;
          }
        }
      }
    }

    return m_configRegistry;
  }

  /**
   * Get Application's config instance.
   *
   * @return config instance
   */
  public static Config getAppConfig() {
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * Get the config instance for the namespace.
   *
   * @param namespace the namespace of the config
   * @return config instance
   */
  public static Config getConfig(String namespace) {
    return s_instance.getManager().getConfig(namespace);
  }

  public static ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    return s_instance.getManager().getConfigFile(namespace, configFileFormat);
  }

  static void setConfig(Config config) {
    setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
  }

  /**
   * Manually set the config for the namespace specified, use with caution.
   *
   * @param namespace the namespace
   * @param config    the config instance
   */
  static void setConfig(String namespace, final Config config) {
    s_instance.getRegistry().register(namespace, new ConfigFactory() {
      @Override
      public Config create(String namespace) {
        return config;
      }

      @Override
      public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        return null;
      }

    });
  }

  static void setConfigFactory(ConfigFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
  }

  /**
   * Manually set the config factory for the namespace specified, use with caution.
   *
   * @param namespace the namespace
   * @param factory   the factory instance
   */
  static void setConfigFactory(String namespace, ConfigFactory factory) {
    s_instance.getRegistry().register(namespace, factory);
  }

  // for test only
  static void setContainer(PlexusContainer m_container) {
    synchronized (s_instance) {
      s_instance.m_container = m_container;
      s_instance.m_configManager = null;
      s_instance.m_configRegistry = null;
    }
  }
}
