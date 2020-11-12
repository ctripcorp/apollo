package com.ctrip.framework.apollo.internals;

/**
 * 注入器接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Injector {

  /**
   * 返回给定注入类型的适当实例
   */
  <T> T getInstance(Class<T> clazz);

  /**
   * 返回给定注入类型和名称的适当实例
   *
   * @param clazz 给定注入类型
   * @param name  给定名称
   * @param <T>   泛型
   * @return 给定注入类型和名称的适当实例
   */
  <T> T getInstance(Class<T> clazz, String name);
}
