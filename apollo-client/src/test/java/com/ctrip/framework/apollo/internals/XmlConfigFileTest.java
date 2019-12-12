package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.google.common.util.concurrent.SettableFuture;

import java.util.LinkedHashMap;
import java.util.Properties;

import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class XmlConfigFileTest {
  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";
  }

  @Test
  public void testWhenHasContent() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    someProperties.put(key, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertEquals(ConfigFileFormat.XML, configFile.getConfigFileFormat());
    assertEquals(someNamespace, configFile.getNamespace());
    assertTrue(configFile.hasContent());
    assertEquals(someValue, configFile.getContent());
  }

  @Test
  public void testWhenHasNoContent() throws Exception {
    when(configRepository.getConfig()).thenReturn(null);

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
  }

  @Test
  public void testWhenConfigRepositoryHasError() throws Exception {
    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    String anotherValue = "anotherValue";
    someProperties.put(key, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertEquals(someValue, configFile.getContent());

    LinkedHashMap anotherProperties = new LinkedHashMap();
    anotherProperties.put(key, anotherValue);

    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
      @Override
      public void onChange(ConfigFileChangeEvent changeEvent) {
        configFileChangeFuture.set(changeEvent);
      }
    };

    configFile.addChangeListener(someListener);

    configFile.onRepositoryChange(someNamespace, anotherProperties);

    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);

    assertEquals(anotherValue, configFile.getContent());
    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(someValue, changeEvent.getOldValue());
    assertEquals(anotherValue, changeEvent.getNewValue());
    assertEquals(PropertyChangeType.MODIFIED, changeEvent.getChangeType());
  }

  @Test
  public void testOnRepositoryChangeWithContentAdded() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";

    when(configRepository.getConfig()).thenReturn(someProperties);

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertEquals(null, configFile.getContent());

    LinkedHashMap anotherProperties = new LinkedHashMap();
    anotherProperties.put(key, someValue);

    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
      @Override
      public void onChange(ConfigFileChangeEvent changeEvent) {
        configFileChangeFuture.set(changeEvent);
      }
    };

    configFile.addChangeListener(someListener);

    configFile.onRepositoryChange(someNamespace, anotherProperties);

    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);

    assertEquals(someValue, configFile.getContent());
    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(null, changeEvent.getOldValue());
    assertEquals(someValue, changeEvent.getNewValue());
    assertEquals(PropertyChangeType.ADDED, changeEvent.getChangeType());
  }

  @Test
  public void testOnRepositoryChangeWithContentDeleted() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    someProperties.put(key, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertEquals(someValue, configFile.getContent());

    LinkedHashMap anotherProperties = new LinkedHashMap();

    final SettableFuture<ConfigFileChangeEvent> configFileChangeFuture = SettableFuture.create();
    ConfigFileChangeListener someListener = new ConfigFileChangeListener() {
      @Override
      public void onChange(ConfigFileChangeEvent changeEvent) {
        configFileChangeFuture.set(changeEvent);
      }
    };

    configFile.addChangeListener(someListener);

    configFile.onRepositoryChange(someNamespace, anotherProperties);

    ConfigFileChangeEvent changeEvent = configFileChangeFuture.get(500, TimeUnit.MILLISECONDS);

    assertEquals(null, configFile.getContent());
    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(someValue, changeEvent.getOldValue());
    assertEquals(null, changeEvent.getNewValue());
    assertEquals(PropertyChangeType.DELETED, changeEvent.getChangeType());
  }

  @Test
  public void testWhenConfigRepositoryHasErrorAndThenRecovered() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    someProperties.put(key, someValue);

    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));

    XmlConfigFile configFile = new XmlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());

    configFile.onRepositoryChange(someNamespace, someProperties);

    assertTrue(configFile.hasContent());
    assertEquals(someValue, configFile.getContent());
  }
}
