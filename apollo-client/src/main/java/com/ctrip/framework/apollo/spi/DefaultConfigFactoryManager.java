package com.ctrip.framework.apollo.spi;

import java.util.Map;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.google.common.collect.Maps;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigFactoryManager implements ConfigFactoryManager {
    private ConfigRegistry m_registry;

    private Map<String, ConfigFactory> m_factories = Maps.newConcurrentMap();

    public DefaultConfigFactoryManager() {
        m_registry = ApolloInjector.getInstance(ConfigRegistry.class);
    }

    @Override
    public ConfigFactory getFactory(String namespace) {
        // step 1: check hacked factory 通过本地注册的方法创建工厂
        ConfigFactory factory = m_registry.getFactory(namespace);

        if (factory != null) {
            return factory;
        }

        // step 2: check cache 通过缓存的方法创建工厂
        factory = m_factories.get(namespace);

        if (factory != null) {
            return factory;
        }

        // step 3: check declared config factory 通过名称+class查找，其实这里默认的都是null
        factory = ApolloInjector.getInstance(ConfigFactory.class, namespace);

        if (factory != null) {
            return factory;
        }

        // step 4: check default config factory 通过默认的方法创建
        factory = ApolloInjector.getInstance(ConfigFactory.class);

        //添加进缓存
        m_factories.put(namespace, factory);

        // factory should not be null
        return factory;
    }
}
