package com.ctrip.framework.apollo.spi;

/**
 * 配置工厂管理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactoryManager {

  /**
   * 获取名称空间配置工厂.
   *
   * @param namespace 指定名称空间
   * @return 名称空间的配置工厂
   */
  ConfigFactory getFactory(String namespace);
}
