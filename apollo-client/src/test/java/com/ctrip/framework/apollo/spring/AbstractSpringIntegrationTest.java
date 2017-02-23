package com.ctrip.framework.apollo.spring;

import com.google.common.collect.Maps;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;

import org.codehaus.plexus.PlexusContainer;
import org.junit.After;
import org.junit.Before;
import org.springframework.util.ReflectionUtils;
import org.unidal.lookup.ComponentTestCase;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractSpringIntegrationTest extends ComponentTestCase {
  private static final Map<String, Config> CONFIG_REGISTRY = Maps.newHashMap();
  private static Method PROPERTY_SOURCES_PROCESSOR_CLEAR;
  private static Method CONFIG_SERVICE_SET_CONTAINER;

  static {
    try {
      PROPERTY_SOURCES_PROCESSOR_CLEAR = PropertySourcesProcessor.class.getDeclaredMethod("reset");
      ReflectionUtils.makeAccessible(PROPERTY_SOURCES_PROCESSOR_CLEAR);
      CONFIG_SERVICE_SET_CONTAINER = ConfigService.class.getDeclaredMethod("setContainer", PlexusContainer.class);
      ReflectionUtils.makeAccessible(CONFIG_SERVICE_SET_CONTAINER);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.tearDown();//clear the container
    super.setUp();
    //as PropertySourcesProcessor has some static states, so we must manually clear its state
    ReflectionUtils.invokeMethod(PROPERTY_SOURCES_PROCESSOR_CLEAR, null);
    //as ConfigService is singleton, so we must manually clear its container
    ReflectionUtils.invokeMethod(CONFIG_SERVICE_SET_CONTAINER, null, getContainer());

    defineComponent(ConfigManager.class, MockConfigManager.class);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    CONFIG_REGISTRY.clear();
  }

  protected void mockConfig(String namespace, Config config) {
    CONFIG_REGISTRY.put(namespace, config);
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
