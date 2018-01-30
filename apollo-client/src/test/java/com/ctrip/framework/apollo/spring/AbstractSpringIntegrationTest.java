package com.ctrip.framework.apollo.spring;

import java.lang.reflect.Method;
import java.util.Map;

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
  private static Method CONFIG_SERVICE_RESET;

  static {
    try {
      PROPERTY_SOURCES_PROCESSOR_CLEAR = PropertySourcesProcessor.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(PROPERTY_SOURCES_PROCESSOR_CLEAR);
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

  protected static void mockConfig(String namespace, Config config) {
    CONFIG_REGISTRY.put(namespace, config);
  }

  protected static void doSetUp() {
    //as PropertySourcesProcessor has some static states, so we must manually clear its state
    ReflectionUtils.invokeMethod(PROPERTY_SOURCES_PROCESSOR_CLEAR, null);
    //as ConfigService is singleton, so we must manually clear its container
    ReflectionUtils.invokeMethod(CONFIG_SERVICE_RESET, null);
    MockInjector.reset();
    MockInjector.setInstance(ConfigManager.class, new MockConfigManager());
  }

  protected static void doTearDown() {
    CONFIG_REGISTRY.clear();
  }

  public static class MockConfigManager implements ConfigManager {

    @Override
    public Config getConfig(String namespace) {
      return CONFIG_REGISTRY.get(namespace);
    }

    @Override
    public ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
      return null;
    }
  }
}
