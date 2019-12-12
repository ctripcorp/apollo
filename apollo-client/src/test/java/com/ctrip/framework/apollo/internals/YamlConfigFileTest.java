package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.build.MockInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.yaml.YamlParser;

import java.util.LinkedHashMap;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YamlConfigFileTest {
  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;
  @Mock
  private YamlParser yamlParser;

  private ConfigSourceType someSourceType;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";

    MockInjector.reset();
    MockInjector.setInstance(YamlParser.class, yamlParser);
  }

  @Test
  public void testWhenHasContent() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someContent = "someKey: 'someValue'";
    someProperties.put(key, someContent);
    someSourceType = ConfigSourceType.LOCAL;

    LinkedHashMap yamlProperties = new LinkedHashMap();
    yamlProperties.put("someKey", "someValue");

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);
    when(yamlParser.yamlToProperties(someContent)).thenReturn(yamlProperties);

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertSame(someContent, configFile.getContent());
    assertSame(yamlProperties, configFile.asProperties());
  }

  @Test
  public void testWhenHasContentWithOrder() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someContent = "someKey: 'someValue'\n someKey2: 'someValue2'";
    someProperties.put(key, someContent);
    someSourceType = ConfigSourceType.LOCAL;

    LinkedHashMap yamlProperties = new LinkedHashMap();
    yamlProperties.put("someKey", "someValue");
    yamlProperties.put("someKey2", "someValue2");

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);
    when(yamlParser.yamlToProperties(someContent)).thenReturn(yamlProperties);

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertSame(someContent, configFile.getContent());
    assertSame(yamlProperties, configFile.asProperties());

    LinkedHashMap parseResult=configFile.asProperties();

    String[] keys =(String[]) parseResult.keySet().toArray(new String[]{});
    assertEquals("someKey",keys[0]);
    assertEquals("someKey2",keys[1]);
  }


  @Test
  public void testWhenHasNoContent() throws Exception {
    when(configRepository.getConfig()).thenReturn(null);

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());

    LinkedHashMap properties = configFile.asProperties();

    assertTrue(properties.isEmpty());
  }

  @Test
  public void testWhenInvalidYamlContent() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someInvalidContent = ",";
    someProperties.put(key, someInvalidContent);
    someSourceType = ConfigSourceType.LOCAL;

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);
    when(yamlParser.yamlToProperties(someInvalidContent)).thenThrow(new RuntimeException("some exception"));

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertSame(someInvalidContent, configFile.getContent());

    Throwable exceptionThrown = null;
    try {
      configFile.asProperties();
    } catch (Throwable ex) {
      exceptionThrown = ex;
    }

    assertTrue(exceptionThrown instanceof ApolloConfigException);
    assertNotNull(exceptionThrown.getCause());
  }

  @Test
  public void testWhenConfigRepositoryHasError() throws Exception {
    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
    assertEquals(ConfigSourceType.NONE, configFile.getSourceType());

    LinkedHashMap properties = configFile.asProperties();

    assertTrue(properties.isEmpty());
  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someKey: 'someValue'";
    String anotherValue = "anotherKey: 'anotherValue'";
    someProperties.put(key, someValue);

    someSourceType = ConfigSourceType.LOCAL;

    LinkedHashMap someYamlProperties = new LinkedHashMap();
    someYamlProperties.put("someKey", "someValue");

    LinkedHashMap anotherYamlProperties = new LinkedHashMap();
    anotherYamlProperties.put("anotherKey", "anotherValue");

    when(configRepository.getConfig()).thenReturn(someProperties);
    when(configRepository.getSourceType()).thenReturn(someSourceType);
    when(yamlParser.yamlToProperties(someValue)).thenReturn(someYamlProperties);
    when(yamlParser.yamlToProperties(anotherValue)).thenReturn(anotherYamlProperties);

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertEquals(someValue, configFile.getContent());
    assertEquals(someSourceType, configFile.getSourceType());
    assertSame(someYamlProperties, configFile.asProperties());

    LinkedHashMap anotherProperties = new LinkedHashMap();
    anotherProperties.put(key, anotherValue);

    ConfigSourceType anotherSourceType = ConfigSourceType.REMOTE;
    when(configRepository.getSourceType()).thenReturn(anotherSourceType);

    configFile.onRepositoryChange(someNamespace, anotherProperties);

    assertEquals(anotherValue, configFile.getContent());
    assertEquals(anotherSourceType, configFile.getSourceType());
    assertSame(anotherYamlProperties, configFile.asProperties());
  }

  @Test
  public void testWhenConfigRepositoryHasErrorAndThenRecovered() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someKey: 'someValue'";
    someProperties.put(key, someValue);

    someSourceType = ConfigSourceType.LOCAL;

    LinkedHashMap someYamlProperties = new LinkedHashMap();
    someYamlProperties.put("someKey", "someValue");

    when(configRepository.getConfig()).thenThrow(new RuntimeException("someError"));
    when(configRepository.getSourceType()).thenReturn(someSourceType);
    when(yamlParser.yamlToProperties(someValue)).thenReturn(someYamlProperties);

    YamlConfigFile configFile = new YamlConfigFile(someNamespace, configRepository);

    assertFalse(configFile.hasContent());
    assertNull(configFile.getContent());
    assertEquals(ConfigSourceType.NONE, configFile.getSourceType());
    assertTrue(configFile.asProperties().isEmpty());

    configFile.onRepositoryChange(someNamespace, someProperties);

    assertTrue(configFile.hasContent());
    assertEquals(someValue, configFile.getContent());
    assertEquals(someSourceType, configFile.getSourceType());
    assertSame(someYamlProperties, configFile.asProperties());
  }
}
