package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 配置管理器接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigManager {

  /**
   * 获取指定命名空间的配置实例
   *
   * @param namespace 名称空间
   * @return 命名空间的配置实例
   */
  Config getConfig(String namespace);

  /**
   * 获取指定命名空间的配置文件实例
   *
   * @param namespace        名称空间
   * @param configFileFormat 配置文件格式
   * @return 指定名称空间的配置文件实例
   */
  ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat);
}
