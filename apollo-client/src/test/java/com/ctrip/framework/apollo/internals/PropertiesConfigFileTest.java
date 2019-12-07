package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertiesConfigFileTest {
  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";
  }

  @Test
  public void testWhenHasContent() throws Exception {
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    someProperties.setProperty(someKey, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    PropertiesConfigFile configFile = new PropertiesConfigFile(someNamespace, configRepository);

    assertEquals(ConfigFileFormat.Properties, configFile.getConfigFileFormat());
    assertEquals(someNamespace, configFile.getNamespace());
    assertTrue(configFile.hasContent());
    assertTrue(configFile.getContent().contains(String.format("%s=%s", someKey, someValue)));
  }

  @Test
  public void testWhenHasNoContent() throws Exception {
    when(configRepository.getConfig()).thenReturn(null);

    PropertiesConfigFile configFile = new PropertiesConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
  }

  @Test
  public void testWhenConfigRepositoryHasError() throws Exception {
    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));

    PropertiesConfigFile configFile = new PropertiesConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    String anotherValue = "anotherValue";
    someProperties.setProperty(someKey, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    PropertiesConfigFile configFile = new PropertiesConfigFile(someNamespace, configRepository);

    assertTrue(configFile.getContent().contains(String.format("%s=%s", someKey, someValue)));

    Properties anotherProperties = new Properties();
    anotherProperties.setProperty(someKey, anotherValue);

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

    assertFalse(configFile.getContent().contains(String.format("%s=%s", someKey, someValue)));
    assertTrue(configFile.getContent().contains(String.format("%s=%s", someKey, anotherValue)));

    assertEquals(someNamespace, changeEvent.getNamespace());
    assertTrue(changeEvent.getOldValue().contains(String.format("%s=%s", someKey, someValue)));
    assertTrue(changeEvent.getNewValue().contains(String.format("%s=%s", someKey, anotherValue)));
    assertEquals(PropertyChangeType.MODIFIED, changeEvent.getChangeType());
  }

  @Test
  public void testWhenConfigRepositoryHasErrorAndThenRecovered() throws Exception {
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    someProperties.setProperty(someKey, someValue);

    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));

    PropertiesConfigFile configFile = new PropertiesConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());

    configFile.onRepositoryChange(someNamespace, someProperties);

    assertTrue(configFile.hasContent());
    assertTrue(configFile.getContent().contains(String.format("%s=%s", someKey, someValue)));
  }
}
