package com.ctrip.framework.apollo.spi;

/**
 * 配置注册器，手动配置注册表，谨慎使用!
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRegistry {

  /**
   * 为指定的名称空间注册配置工厂
   *
   * @param namespace 指定的名称空间
   * @param factory   指定名称空间的配置工厂
   */
  void register(String namespace, ConfigFactory factory);

  /**
   * 获取名称空间的已注册配置工厂.
   *
   * @param namespace 指定的名称空间
   * @return 此名称空间注册的工厂
   */
  ConfigFactory getFactory(String namespace);
}
