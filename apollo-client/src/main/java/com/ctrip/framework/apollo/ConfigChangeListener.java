package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * 配置变更监听接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigChangeListener {

  /**
   * 当命名空间有任何配置更改时调用.
   *
   * @param changeEvent 更改的事件
   */
  void onChange(ConfigChangeEvent changeEvent);
}
