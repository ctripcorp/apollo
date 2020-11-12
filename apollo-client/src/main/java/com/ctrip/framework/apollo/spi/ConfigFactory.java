package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 配置工厂
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactory {

  /**
   * 为名称空间创建配置实例
   *
   * @param namespace 指定的名称空间
   * @return 新创建的配置实例
   */
  Config create(String namespace);

  /**
   * 为名称空间创建配置文件实例
   *
   * @param namespace 指定的名称空间
   * @return 新创建的配置文件实例
   */
  ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat);
}
