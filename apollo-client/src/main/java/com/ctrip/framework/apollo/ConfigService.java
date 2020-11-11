package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigRegistry;

/**
 * 客户端配置服务，作为配置使用的入口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {

  /**
   * 单例
   */
  private static final ConfigService s_instance = new ConfigService();

  /**
   * 配置管理器
   */
  private volatile ConfigManager m_configManager;
  /**
   * 配置注册器
   */
  private volatile ConfigRegistry m_configRegistry;

  /**
   * 获取配置管理器
   *
   * @return ConfigManager对象
   */
  private ConfigManager getManager() {
    // 若 ConfigManager 未初始化，进行获得
    if (m_configManager == null) {
      synchronized (this) {
        if (m_configManager == null) {
          m_configManager = ApolloInjector.getInstance(ConfigManager.class);
        }
      }
    }
    // 返回 ConfigManager
    return m_configManager;
  }

  /**
   * 获取配置注册器
   *
   * @return ConfigManager对象
   */
  private ConfigRegistry getRegistry() {
    // 若 ConfigRegistry 未初始化，进行获得
    if (m_configRegistry == null) {
      synchronized (this) {
        if (m_configRegistry == null) {
          m_configRegistry = ApolloInjector.getInstance(ConfigRegistry.class);
        }
      }
    }
    // 返回 ConfigRegistry
    return m_configRegistry;
  }

  /**
   * 获取Application的配置实例.
   *
   * @return 配置实例
   */
  public static Config getAppConfig() {
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * 获取指定名称空间的配置实例.
   *
   * @param namespace 指定配置的名称空间
   * @return 配置实例
   */
  public static Config getConfig(String namespace) {
    return s_instance.getManager().getConfig(namespace);
  }

  /**
   * 获取配置文件
   *
   * @param namespace        名称空间
   * @param configFileFormat 配置文件格式枚举
   * @return 配置文件实例
   */
  public static ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    return s_instance.getManager().getConfigFile(namespace, configFileFormat);
  }

  /**
   * 设置配置文件实例
   *
   * @param config 配置实例
   */
  static void setConfig(Config config) {
    setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
  }

  /**
   * 手动设置指定名称空间的配置，请谨慎使用。
   *
   * @param namespace 指定的名称空间
   * @param config    指定的配置实例
   */
  static void setConfig(String namespace, final Config config) {
    // 注册
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

  /**
   * 设置 ConfigFactory 对象
   *
   * @param factory 配置工厂实例
   */
  static void setConfigFactory(ConfigFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
  }

  /**
   * 为指定的名称空间手动设置配置工厂，请小心使用
   *
   * @param namespace 指定的名称空间
   * @param factory   指定的配置工厂
   */
  static void setConfigFactory(String namespace, ConfigFactory factory) {
    s_instance.getRegistry().register(namespace, factory);
  }

  /**
   * 重置
   */
  static void reset() {
    // 设置为null
    synchronized (s_instance) {
      s_instance.m_configManager = null;
      s_instance.m_configRegistry = null;
    }
  }
}
