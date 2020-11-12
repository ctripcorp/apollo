package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;
import java.util.Properties;

/**
 * 纯文本 ConfigFile 抽象类,例如 xml yaml 等等
 *
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
    // 获取配置文本
    return m_configProperties.get().getProperty(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  public boolean hasContent() {
    if (m_configProperties.get() == null) {
      return false;
    }
    // 判断是否有内容
    return m_configProperties.get().containsKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  protected void update(Properties newProperties) {
    // 更新内容
    m_configProperties.set(newProperties);
  }
}
