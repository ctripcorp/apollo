package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.enums.ConfigSourceType;

import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleConfigTest {
  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;
  private ConfigSourceType someSourceType;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";
  }

  @Test
  public void testGetProperty() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String someKey = "someKey";
    String someValue = "someValue";
    someProperties.put(someKey, someValue);

    someSourceType = ConfigSourceType.LOCAL;

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);

    SimpleConfig config = new SimpleConfig(someNamespace, configRepository);

    assertEquals(someValue, config.getProperty(someKey, null));
    assertEquals(someSourceType, config.getSourceType());
  }

  @Test
  public void testLoadConfigFromConfigRepositoryError() throws Exception {
    String someKey = "someKey";
    String anyValue = "anyValue" + Math.random();

    when(configRepository.getConfig()).thenThrow(mock(RuntimeException.class));

    Config config = new SimpleConfig(someNamespace, configRepository);

    assertEquals(anyValue, config.getProperty(someKey, anyValue));
    assertEquals(ConfigSourceType.NONE, config.getSourceType());
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String someKey = "someKey";
    String someValue = "someValue";
    String anotherKey = "anotherKey";
    String anotherValue = "anotherValue";
    someProperties.putAll(ImmutableMap.of(someKey, someValue, anotherKey, anotherValue));

    LinkedHashMap anotherProperties = new LinkedHashMap();
    String newKey = "newKey";
    String newValue = "newValue";
    String someValueNew = "someValueNew";
    anotherProperties.putAll(ImmutableMap.of(someKey, someValueNew, newKey, newValue));

    someSourceType = ConfigSourceType.LOCAL;

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);

    final SettableFuture<ConfigChangeEvent> configChangeFuture = SettableFuture.create();
    ConfigChangeListener someListener = new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        configChangeFuture.set(changeEvent);
      }
    };

    SimpleConfig config = new SimpleConfig(someNamespace, configRepository);

    assertEquals(someSourceType, config.getSourceType());

    config.addChangeListener(someListener);

    ConfigSourceType anotherSourceType = ConfigSourceType.REMOTE;
    when(configRepository.getSourceType()).thenReturn(anotherSourceType);

    config.onRepositoryChange(someNamespace, anotherProperties);

    ConfigChangeEvent changeEvent = configChangeFuture.get(500, TimeUnit.MILLISECONDS);

    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(3, changeEvent.changedKeys().size());

    ConfigChange someKeyChange = changeEvent.getChange(someKey);
    assertEquals(someValue, someKeyChange.getOldValue());
    assertEquals(someValueNew, someKeyChange.getNewValue());
    assertEquals(PropertyChangeType.MODIFIED, someKeyChange.getChangeType());

    ConfigChange anotherKeyChange = changeEvent.getChange(anotherKey);
    assertEquals(anotherValue, anotherKeyChange.getOldValue());
    assertEquals(null, anotherKeyChange.getNewValue());
    assertEquals(PropertyChangeType.DELETED, anotherKeyChange.getChangeType());

    ConfigChange newKeyChange = changeEvent.getChange(newKey);
    assertEquals(null, newKeyChange.getOldValue());
    assertEquals(newValue, newKeyChange.getNewValue());
    assertEquals(PropertyChangeType.ADDED, newKeyChange.getChangeType());

    assertEquals(anotherSourceType, config.getSourceType());
  }
}
