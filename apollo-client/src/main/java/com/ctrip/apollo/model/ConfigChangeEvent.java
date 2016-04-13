package com.ctrip.apollo.model;

import java.util.Map;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigChangeEvent {
  private final String m_namespace;
  private final Map<String, ConfigChange> changes;

  public ConfigChangeEvent(String m_namespace,
                           Map<String, ConfigChange> changes) {
    this.m_namespace = m_namespace;
    this.changes = changes;
  }

  public Set<String> changedKeys() {
    return changes.keySet();
  }

  public ConfigChange getChange(String key) {
    return changes.get(key);
  }

  /**
   * Please note that the returned Map is immutable
   * @return changes
   */
  public Map<String, ConfigChange> getChanges() {
    return changes;
  }

  public String getNamespace() {
    return m_namespace;
  }
}
