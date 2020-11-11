package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 类型为 .yml 的 ConfigFile 实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class YmlConfigFile extends YamlConfigFile {

  public YmlConfigFile(String namespace, ConfigRepository configRepository) {
    super(namespace, configRepository);
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.YML;
  }
}
