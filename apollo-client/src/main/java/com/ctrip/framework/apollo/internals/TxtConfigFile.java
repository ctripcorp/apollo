package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 类型为 .txt 的 ConfigFile 实现类
 */
public class TxtConfigFile extends PlainTextConfigFile {

  public TxtConfigFile(String namespace, ConfigRepository configRepository) {
    super(namespace, configRepository);
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.TXT;
  }
}
