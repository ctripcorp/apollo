package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * 默认配置工厂管理器实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigFactoryManager implements ConfigFactoryManager {

  /**
   * 配置注册器
   */
  private ConfigRegistry m_registry;
  /**
   * 配置工厂对象的缓存
   */
  private Map<String, ConfigFactory> m_factories = Maps.newConcurrentMap();

  public DefaultConfigFactoryManager() {
    m_registry = ApolloInjector.getInstance(ConfigRegistry.class);
  }

  @Override
  public ConfigFactory getFactory(String namespace) {
    // step 1: 检查注册器工厂
    ConfigFactory factory = m_registry.getFactory(namespace);

    if (factory != null) {
      return factory;
    }

    // step 2: 检查缓存
    factory = m_factories.get(namespace);

    if (factory != null) {
      return factory;
    }

    // step 3: 检查声明的配置工厂
    factory = ApolloInjector.getInstance(ConfigFactory.class, namespace);

    if (factory != null) {
      return factory;
    }

    // step 4: 检查默认配置工厂
    factory = ApolloInjector.getInstance(ConfigFactory.class);

    // 更新到缓存中
    m_factories.put(namespace, factory);

    // 工厂不应为空
    return factory;
  }
}
