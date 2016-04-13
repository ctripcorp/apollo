package com.ctrip.apollo;

import com.ctrip.apollo.model.ConfigChangeEvent;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigChangeListener {
  public void onChange(ConfigChangeEvent changeEvent);
}
