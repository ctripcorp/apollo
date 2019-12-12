package com.ctrip.framework.apollo.internals;

import java.util.LinkedHashMap;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RepositoryChangeListener {
  /**
   * Invoked when config repository changes.
   * @param namespace the namespace of this repository change
   * @param newProperties the properties after change
   */
  public void onRepositoryChange(String namespace, LinkedHashMap newProperties);
}
