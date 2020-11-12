package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigFactoryManager;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * 默认配置管理器实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigManager implements ConfigManager {

  /**
   * 配置工厂管理器
   */
  private ConfigFactoryManager m_factoryManager;
  /**
   * Config 对象的缓存
   */
  private Map<String, Config> m_configs = Maps.newConcurrentMap();
  /**
   * ConfigFile 对象的缓存
   */
  private Map<String, ConfigFile> m_configFiles = Maps.newConcurrentMap();

  public DefaultConfigManager() {
    m_factoryManager = ApolloInjector.getInstance(ConfigFactoryManager.class);
  }

  @Override
  public Config getConfig(String namespace) {
    // 获得 Config 对象
    Config config = m_configs.get(namespace);
    // 若不存在，进行创建
    if (config == null) {
      synchronized (this) {
        // 获得 Config 对象
        config = m_configs.get(namespace);
        // 获得对应的 ConfigFactory 对象
        if (config == null) {
          ConfigFactory factory = m_factoryManager.getFactory(namespace);
          // 创建 Config 对象
          config = factory.create(namespace);
          // 添加到缓存
          m_configs.put(namespace, config);
        }
      }
    }

    return config;
  }

  @Override
  public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    // 拼接 Namespace 名字
    String namespaceFileName = String.format("%s.%s", namespace, configFileFormat.getValue());
    // 获得 ConfigFile 对象
    ConfigFile configFile = m_configFiles.get(namespaceFileName);
    // 若不存在，进行创建
    if (configFile == null) {
      synchronized (this) {
        // 获得 ConfigFile 对象
        configFile = m_configFiles.get(namespaceFileName);
        // 若不存在，进行创建
        if (configFile == null) {
          // 获得对应的 ConfigFactory 对象
          ConfigFactory factory = m_factoryManager.getFactory(namespaceFileName);
          // 创建 ConfigFile 对象
          configFile = factory.createConfigFile(namespaceFileName, configFileFormat);
          // 添加到缓存
          m_configFiles.put(namespaceFileName, configFile);
        }
      }
    }
    return configFile;
  }
}
