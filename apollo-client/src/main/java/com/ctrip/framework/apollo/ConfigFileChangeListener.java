package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;

/**
 * 配置文件变更监听器接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFileChangeListener {

  /**
   * 当命名空间有任何配置更改时调用
   *
   * @param changeEvent 更改的事件
   */
  void onChange(ConfigFileChangeEvent changeEvent);
}
