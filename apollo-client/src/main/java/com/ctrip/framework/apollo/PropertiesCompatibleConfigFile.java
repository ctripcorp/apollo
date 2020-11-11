package com.ctrip.framework.apollo;

import java.util.Properties;

/**
 * Properties兼容的配置文件，例如yaml
 *
 * @since 1.3.0
 */
public interface PropertiesCompatibleConfigFile extends ConfigFile {

  /**
   * 返回转换后的Propertes对象
   *
   * @return 配置文件的Propertes类型
   * @throws RuntimeException 如果内容无法转换为属性,抛出
   */
  Properties asProperties();
}
