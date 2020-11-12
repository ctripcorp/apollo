package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 类型为 .xml 的 ConfigFile 实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlConfigFile extends PlainTextConfigFile {

  public XmlConfigFile(String namespace, ConfigRepository configRepository) {
    super(namespace, configRepository);
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.XML;
  }
}
