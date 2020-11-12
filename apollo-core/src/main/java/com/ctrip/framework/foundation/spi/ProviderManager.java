package com.ctrip.framework.foundation.spi;

import com.ctrip.framework.foundation.spi.provider.Provider;

/**
 * 提供者管理器.
 */
public interface ProviderManager {

  /**
   * 返回具有给定名称的属性值,如果不存在返回 {@code defaultValue}
   *
   * @param name         给定的名称
   * @param defaultValue 未找到名称或发生任何错误时的默认值
   * @return 具有给定名称的属性值
   */
  String getProperty(String name, String defaultValue);

  /**
   * 获取指定Class的提供者对象
   *
   * @return 获取指定Class的提供者对象
   */
  <T extends Provider> T provider(Class<T> clazz);
}
