package com.ctrip.apollo;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.spi.ConfigFactory;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import static org.junit.Assert.assertEquals;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigServiceTest extends ComponentTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigService.setContainer(getContainer());
  }

  @Test
  public void testHackConfig() {
    String someNamespace = "hack";
    String someKey = "first";
    ConfigService.setConfig(new MockConfig(someNamespace));

    Config config = ConfigService.getConfig();

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  @Test
  public void testHackConfigFactory() throws Exception {
    String someKey = "someKey";
    ConfigService.setConfigFactory(new MockConfigFactory());

    Config config = ConfigService.getConfig();

    assertEquals(ConfigConsts.NAMESPACE_APPLICATION + ":" + someKey,
        config.getProperty(someKey, null));
  }

  @Test
  public void testMockConfigFactory() throws Exception {
    String someNamespace = "mock";
    String someKey = "someKey";
    defineComponent(ConfigFactory.class, someNamespace, MockConfigFactory.class);

    Config config = ConfigService.getConfig(someNamespace);

    assertEquals(someNamespace + ":" + someKey, config.getProperty(someKey, null));
    assertEquals(null, config.getProperty("unknown", null));
  }

  private static class MockConfig implements Config {
    private final String m_namespace;

    public MockConfig(String namespace) {
      m_namespace = namespace;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
      if (key.equals("unknown")) {
        return null;
      }

      return m_namespace + ":" + key;
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {

    }
  }

  public static class MockConfigFactory implements ConfigFactory {
    @Override
    public Config create(String namespace) {
      return new MockConfig(namespace);
    }
  }
}
