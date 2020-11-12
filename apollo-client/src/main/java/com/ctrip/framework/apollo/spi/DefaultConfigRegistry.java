package com.ctrip.framework.apollo.spi;

import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 ConfigFactory 管理器实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class DefaultConfigRegistry implements ConfigRegistry {

  /**
   * 配置工厂实例Map，key：名称空间，value:配置工厂
   */
  private Map<String, ConfigFactory> instances = Maps.newConcurrentMap();

  @Override
  public void register(String namespace, ConfigFactory factory) {
    // 覆盖的情况，打印警告日志
    if (instances.containsKey(namespace)) {
      log.warn("ConfigFactory({}) is overridden by {}!", namespace, factory.getClass());
    }

    instances.put(namespace, factory);
  }

  @Override
  public ConfigFactory getFactory(String namespace) {
    // 获取配置工厂
    return instances.get(namespace);
  }
}
