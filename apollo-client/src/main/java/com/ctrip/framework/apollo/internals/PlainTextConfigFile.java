package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;

import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class PlainTextConfigFile extends AbstractConfigFile {

  public PlainTextConfigFile(String namespace, ConfigRepository configRepository) {
    super(namespace, configRepository);
  }

  @Override
  public String getContent() {
    if (!this.hasContent()) {
      return null;
    }
    return m_configProperties.get().getProperty(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  public boolean hasContent() {
    if (m_configProperties.get() == null) {
      return false;
    }
    return m_configProperties.get().containsKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  protected void update(Properties newProperties) {
    m_configProperties.set(newProperties);
  }
}
