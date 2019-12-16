package com.ctrip.framework.apollo.model;

import java.util.Map;
import java.util.Set;

/**
 * A change event when a namespace's config is changed.
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigChangeEvent {
  private final String m_namespace;
  private final Map<String, ConfigChange> m_changes;

  /**
   * Constructor.
   * @param namespace the namespace of this change
   * @param changes the actual changes
   */
  public ConfigChangeEvent(String namespace,
                           Map<String, ConfigChange> changes) {
    m_namespace = namespace;
    m_changes = changes;
  }

  /**
   * Get the keys changed.
   * @return the list of the keys
   */
  public Set<String> changedKeys() {
    return m_changes.keySet();
  }

  /**
   * Get a specific change instance for the key specified.
   * @param key the changed key
   * @return the change instance
   */
  public ConfigChange getChange(String key) {
    return m_changes.get(key);
  }

  /**
   * Check whether the specified key is changed
   * @param key the key
   * @return true if the key is changed, false otherwise.
   */
  public boolean isChanged(String key) {
    return m_changes.containsKey(key);
  }

  /**
   * Get the namespace of this change event.
   * @return the namespace
   */
  public String getNamespace() {
    return m_namespace;
  }

  public Map<String, ConfigChange> getChangeValue(){
        return m_changes;
    }
}
