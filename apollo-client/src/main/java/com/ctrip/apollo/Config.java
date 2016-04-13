package com.ctrip.apollo;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {
  /**
   * Return the property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value
   */
  public String getProperty(String key, String defaultValue);

  public void addChangeListener(ConfigChangeListener listener);
}
