package com.ctrip.framework.apollo.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.SimpleConfig;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.util.ConfigUtil;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.springframework.util.ReflectionUtils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.build.MockInjector;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import com.google.common.collect.Maps;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractSpringIntegrationTest {
  private static final Map<String, Config> CONFIG_REGISTRY = Maps.newHashMap();
  private static Method PROPERTY_SOURCES_PROCESSOR_CLEAR;
  private static Method SPRING_VALUE_DEFINITION_PROCESS_CLEAR;
  private static Method CONFIG_SERVICE_RESET;

  static {
    try {
      PROPERTY_SOURCES_PROCESSOR_CLEAR = PropertySourcesProcessor.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(PROPERTY_SOURCES_PROCESSOR_CLEAR);
      SPRING_VALUE_DEFINITION_PROCESS_CLEAR = SpringValueDefinitionProcessor.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(SPRING_VALUE_DEFINITION_PROCESS_CLEAR);
      CONFIG_SERVICE_RESET = ConfigService.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(CONFIG_SERVICE_RESET);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws Exception {
    doSetUp();
  }

  @After
  public void tearDown() throws Exception {
    doTearDown();
  }

  protected SimpleConfig prepareConfig(String namespaceName, Properties properties) {
    ConfigRepository configRepository = mock(ConfigRepository.class);

    when(configRepository.getConfig()).thenReturn(properties);

    SimpleConfig config = new SimpleConfig(ConfigConsts.NAMESPACE_APPLICATION, configRepository);

    mockConfig(namespaceName, config);

    return config;
  }

  protected Properties assembleProperties(String key, String value) {
    Properties properties = new Properties();
    properties.setProperty(key, value);

    return properties;
  }

  protected Properties assembleProperties(String key, String value, String key2, String value2) {
    Properties properties = new Properties();
    properties.setProperty(key, value);
    properties.setProperty(key2, value2);

    return properties;
  }

  protected Properties assembleProperties(String key, String value, String key2, String value2,
      String key3, String value3) {

    Properties properties = new Properties();
    properties.setProperty(key, value);
    properties.setProperty(key2, value2);
    properties.setProperty(key3, value3);

    return properties;
  }

  protected Date assembleDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
    Calendar date = Calendar.getInstance();
    date.set(year, month - 1, day, hour, minute, second); //Month in Calendar is 0 based
    date.set(Calendar.MILLISECOND, millisecond);

    return date.getTime();
  }


  protected static void mockConfig(String namespace, Config config) {
    CONFIG_REGISTRY.put(namespace, config);
  }

  protected static void doSetUp() {
    //as PropertySourcesProcessor has some static states, so we must manually clear its state
    ReflectionUtils.invokeMethod(PROPERTY_SOURCES_PROCESSOR_CLEAR, null);
    //as SpringValueDefinitionProcessor has some static states, so we must manually clear its state
    ReflectionUtils.invokeMethod(SPRING_VALUE_DEFINITION_PROCESS_CLEAR, null);
    //as ConfigService is singleton, so we must manually clear its container
    ReflectionUtils.invokeMethod(CONFIG_SERVICE_RESET, null);
    MockInjector.reset();
    MockInjector.setInstance(ConfigManager.class, new MockConfigManager());
  }

  protected static void doTearDown() {
    CONFIG_REGISTRY.clear();
  }

  private static class MockConfigManager implements ConfigManager {

    @Override
    public Set<String> getAllNamespaces() {
      return Collections.unmodifiableSet(CONFIG_REGISTRY.keySet());
    }
    
    @Override
    public Config getConfig(String namespace) {
      return CONFIG_REGISTRY.get(namespace);
    }

    @Override
    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
      return null;
    }
  }

  protected static class MockConfigUtil extends ConfigUtil {

    private boolean isAutoUpdateInjectedSpringProperties;

    public void setAutoUpdateInjectedSpringProperties(boolean autoUpdateInjectedSpringProperties) {
      isAutoUpdateInjectedSpringProperties = autoUpdateInjectedSpringProperties;
    }

    @Override
    public boolean isAutoUpdateInjectedSpringPropertiesEnabled() {
      return isAutoUpdateInjectedSpringProperties;
    }
  }
}
