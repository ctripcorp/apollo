package com.ctrip.apollo.internals;

import com.google.common.collect.ImmutableMap;

import com.ctrip.apollo.ConfigChangeListener;
import com.ctrip.apollo.enums.PropertyChangeType;
import com.ctrip.apollo.model.ConfigChange;
import com.ctrip.apollo.model.ConfigChangeEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleConfigTest {
  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";
  }

  @Test
  public void testGetProperty() throws Exception {
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    someProperties.setProperty(someKey, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    SimpleConfig config = new SimpleConfig(someNamespace, configRepository);

    assertEquals(someValue, config.getProperty(someKey, null));
  }

  @Test(expected = RuntimeException.class)
  public void testLoadConfigFromConfigRepositoryError() throws Exception {
    when(configRepository.getConfig()).thenThrow(Throwable.class);

    new SimpleConfig(someNamespace, configRepository);
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    String anotherKey = "anotherKey";
    String anotherValue = "anotherValue";
    someProperties.putAll(ImmutableMap.of(someKey, someValue, anotherKey, anotherValue));

    Properties anotherProperties = new Properties();
    String newKey = "newKey";
    String newValue = "newValue";
    String someValueNew = "someValueNew";
    anotherProperties.putAll(ImmutableMap.of(someKey, someValueNew, newKey, newValue));

    when(configRepository.getConfig()).thenReturn(someProperties);

    ConfigChangeListener someListener = mock(ConfigChangeListener.class);

    SimpleConfig config = new SimpleConfig(someNamespace, configRepository);
    config.addChangeListener(someListener);

    config.onRepositoryChange(someNamespace, anotherProperties);

    ArgumentCaptor<ConfigChangeEvent> captor = ArgumentCaptor.forClass(ConfigChangeEvent.class);

    verify(someListener, times(1)).onChange(captor.capture());

    ConfigChangeEvent changeEvent = captor.getValue();

    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(3, changeEvent.getChanges().size());

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
    assertEquals(PropertyChangeType.NEW, newKeyChange.getChangeType());
  }
}
