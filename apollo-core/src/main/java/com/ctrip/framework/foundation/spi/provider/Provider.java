package com.ctrip.framework.foundation.spi.provider;

/**
 * 供应器
 */
public interface Provider {

  /**
   * 获取当前供应器的类型Class
   *
   * @return 当前提供者的类型Class
   */
  Class<? extends Provider> getType();

  /**
   * 返回具有给定名称的属性值,如果不存在返回 {@code defaultValue}
   *
   * @param name         给定的名称
   * @param defaultValue 未找到名称或发生任何错误时的默认值
   * @return 具有给定名称的属性值
   */
  String getProperty(String name, String defaultValue);

  /**
   * 初始化供应器
   */
  void initialize();
}
