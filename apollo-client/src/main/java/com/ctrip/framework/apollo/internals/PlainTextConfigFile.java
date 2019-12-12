package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;

import java.util.LinkedHashMap;

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
    return (String) m_configProperties.get().get(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  public boolean hasContent() {
    if (m_configProperties.get() == null) {
      return false;
    }
    return m_configProperties.get().containsKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  protected void update(LinkedHashMap newProperties) {
    m_configProperties.set(newProperties);
  }
}
