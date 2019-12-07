package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesCompatibleFileConfigRepositoryTest {

  @Mock
  private PropertiesCompatibleConfigFile configFile;

  private String someNamespaceName;

  @Mock
  private Properties someProperties;

  @Before
  public void setUp() throws Exception {
    someNamespaceName = "someNamespaceName";
    when(configFile.getNamespace()).thenReturn(someNamespaceName);
    when(configFile.asProperties()).thenReturn(someProperties);
  }

  @Test
  public void testGetConfig() throws Exception {
    PropertiesCompatibleFileConfigRepository configFileRepository = new PropertiesCompatibleFileConfigRepository(
        configFile);

    assertSame(someProperties, configFileRepository.getConfig());
    verify(configFile, times(1)).addChangeListener(configFileRepository);
  }

  @Test
  public void testGetConfigFailedAndThenRecovered() throws Exception {
    RuntimeException someException = new RuntimeException("some exception");

    when(configFile.asProperties()).thenThrow(someException);

    PropertiesCompatibleFileConfigRepository configFileRepository = new PropertiesCompatibleFileConfigRepository(
        configFile);

    Throwable exceptionThrown = null;
    try {
      configFileRepository.getConfig();
    } catch (Throwable ex) {
      exceptionThrown = ex;
    }

    assertSame(someException, exceptionThrown);

    // recovered
    reset(configFile);

    Properties someProperties = mock(Properties.class);

    when(configFile.asProperties()).thenReturn(someProperties);

    assertSame(someProperties, configFileRepository.getConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetConfigWithConfigFileReturnNullProperties() throws Exception {
    when(configFile.asProperties()).thenReturn(null);

    PropertiesCompatibleFileConfigRepository configFileRepository = new PropertiesCompatibleFileConfigRepository(
        configFile);

    configFileRepository.getConfig();
  }

  @Test
  public void testGetSourceType() throws Exception {
    ConfigSourceType someType = ConfigSourceType.REMOTE;

    when(configFile.getSourceType()).thenReturn(someType);

    PropertiesCompatibleFileConfigRepository configFileRepository = new PropertiesCompatibleFileConfigRepository(
        configFile);

    assertSame(someType, configFileRepository.getSourceType());
  }

  @Test
  public void testOnChange() throws Exception {
    Properties anotherProperties = mock(Properties.class);
    ConfigFileChangeEvent someChangeEvent = mock(ConfigFileChangeEvent.class);

    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);

    PropertiesCompatibleFileConfigRepository configFileRepository = new PropertiesCompatibleFileConfigRepository(
        configFile);

    configFileRepository.addChangeListener(someListener);

    assertSame(someProperties, configFileRepository.getConfig());

    when(configFile.asProperties()).thenReturn(anotherProperties);

    configFileRepository.onChange(someChangeEvent);

    assertSame(anotherProperties, configFileRepository.getConfig());
    verify(someListener, times(1)).onRepositoryChange(someNamespaceName, anotherProperties);
  }
}
