package com.ctrip.apollo.internals;

import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRepository {
  /**
   * Get the config from this repository
   * @return
   */
  public Properties getConfig();

  /**
   * Set the fallback repo for this repository
   * @param fallbackConfigRepository
   */
  public void setFallback(ConfigRepository fallbackConfigRepository);

  public void addChangeListener(RepositoryChangeListener listener);

  public void removeChangeListener(RepositoryChangeListener listener);
}
