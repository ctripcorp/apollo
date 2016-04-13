package com.ctrip.apollo.internals;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.ConfigChangeListener;
import com.ctrip.apollo.enums.PropertyChangeType;
import com.ctrip.apollo.model.ConfigChange;
import com.ctrip.apollo.model.ConfigChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfig implements Config {
  private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);
  private List<ConfigChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

  @Override
  public void addChangeListener(ConfigChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  protected void fireConfigChange(ConfigChangeEvent changeEvent) {
    for (ConfigChangeListener listener : m_listeners) {
      try {
        listener.onChange(changeEvent);
      } catch (Throwable t) {
        logger.error("Failed to invoke config change listener {}", listener.getClass(), t);
      }
    }
  }

  List<ConfigChange> calcPropertyChanges(Properties previous,
                                         Properties current) {
    if (previous == null) {
      previous = new Properties();
    }

    if (current == null) {
      current = new Properties();
    }

    Set<String> previousKeys = previous.stringPropertyNames();
    Set<String> currentKeys = current.stringPropertyNames();

    Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
    Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
    Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

    List<ConfigChange> changes = Lists.newArrayList();

    for (String newKey : newKeys) {
      changes.add(new ConfigChange(newKey, null, current.getProperty(newKey), PropertyChangeType.NEW));
    }

    for (String removedKey : removedKeys) {
      changes.add(new ConfigChange(removedKey, previous.getProperty(removedKey), null,
          PropertyChangeType.DELETED));
    }

    for (String commonKey : commonKeys) {
      String previousValue = previous.getProperty(commonKey);
      String currentValue = current.getProperty(commonKey);
      if (Objects.equal(previousValue, currentValue)) {
        continue;
      }
      changes.add(new ConfigChange(commonKey, previousValue,
          currentValue, PropertyChangeType.MODIFIED));
    }

    return changes;
  }
}
