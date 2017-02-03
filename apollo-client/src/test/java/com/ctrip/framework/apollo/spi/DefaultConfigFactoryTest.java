package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.JsonConfigFile;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.PropertiesConfigFile;
import com.ctrip.framework.apollo.internals.XmlConfigFile;
import com.ctrip.framework.apollo.internals.YamlConfigFile;
import com.ctrip.framework.apollo.internals.YmlConfigFile;
import com.ctrip.framework.apollo.util.ConfigUtil;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.unidal.lookup.ComponentTestCase;

import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigFactoryTest extends ComponentTestCase {
  private DefaultConfigFactory defaultConfigFactory;
  private static String someAppId;
  private static Env someEnv;

  @Before
  public void setUp() throws Exception {
    super.tearDown();//clear the container
    super.setUp();
    someAppId = "someId";
    someEnv = Env.DEV;
    defineComponent(ConfigUtil.class, MockConfigUtil.class);
    defaultConfigFactory = spy((DefaultConfigFactory) lookup(ConfigFactory.class));
  }

  @Test
  public void testCreate() throws Exception {
    String someNamespace = "someName";
    Properties someProperties = new Properties();
    String someKey = "someKey";
    String someValue = "someValue";
    someProperties.setProperty(someKey, someValue);

    LocalFileConfigRepository someLocalConfigRepo = mock(LocalFileConfigRepository.class);
    when(someLocalConfigRepo.getConfig()).thenReturn(someProperties);

    doReturn(someLocalConfigRepo).when(defaultConfigFactory).createLocalConfigRepository(someNamespace);

    Config result = defaultConfigFactory.create(someNamespace);

    assertThat("DefaultConfigFactory should create DefaultConfig", result,
        is(instanceOf(DefaultConfig.class)));
    assertEquals(someValue, result.getProperty(someKey, null));
  }

  @Test
  public void testCreateLocalConfigRepositoryInLocalDev() throws Exception {
    String someNamespace = "someName";
    someEnv = Env.LOCAL;

    LocalFileConfigRepository localFileConfigRepository =
        defaultConfigFactory.createLocalConfigRepository(someNamespace);

    assertNull(ReflectionTestUtils.getField(localFileConfigRepository, "m_upstream"));
  }

  @Test
  public void testCreateConfigFile() throws Exception {
    String someNamespace = "someName";
    String anotherNamespace = "anotherName";
    String yetAnotherNamespace = "yetAnotherNamespace";
    Properties someProperties = new Properties();

    LocalFileConfigRepository someLocalConfigRepo = mock(LocalFileConfigRepository.class);
    when(someLocalConfigRepo.getConfig()).thenReturn(someProperties);

    doReturn(someLocalConfigRepo).when(defaultConfigFactory).createLocalConfigRepository(someNamespace);
    doReturn(someLocalConfigRepo).when(defaultConfigFactory).createLocalConfigRepository(anotherNamespace);
    doReturn(someLocalConfigRepo).when(defaultConfigFactory).createLocalConfigRepository(yetAnotherNamespace);

    ConfigFile propertyConfigFile =
        defaultConfigFactory.createConfigFile(someNamespace, ConfigFileFormat.Properties);
    ConfigFile xmlConfigFile =
        defaultConfigFactory.createConfigFile(anotherNamespace, ConfigFileFormat.XML);
    ConfigFile jsonConfigFile =
        defaultConfigFactory.createConfigFile(yetAnotherNamespace, ConfigFileFormat.JSON);
    ConfigFile ymlConfigFile = defaultConfigFactory.createConfigFile(someNamespace,
        ConfigFileFormat.YML);
    ConfigFile yamlConfigFile = defaultConfigFactory.createConfigFile(someNamespace,
        ConfigFileFormat.YAML);

    assertThat("Should create PropertiesConfigFile for properties format", propertyConfigFile, is(instanceOf(
        PropertiesConfigFile.class)));
    assertEquals(someNamespace, propertyConfigFile.getNamespace());

    assertThat("Should create XmlConfigFile for xml format", xmlConfigFile, is(instanceOf(
        XmlConfigFile.class)));
    assertEquals(anotherNamespace, xmlConfigFile.getNamespace());

    assertThat("Should create JsonConfigFile for json format", jsonConfigFile, is(instanceOf(
        JsonConfigFile.class)));
    assertEquals(yetAnotherNamespace, jsonConfigFile.getNamespace());

    assertThat("Should create YmlConfigFile for yml format", ymlConfigFile, is(instanceOf(
        YmlConfigFile.class)));
    assertEquals(someNamespace, ymlConfigFile.getNamespace());

    assertThat("Should create YamlConfigFile for yaml format", yamlConfigFile, is(instanceOf(
        YamlConfigFile.class)));
    assertEquals(someNamespace, yamlConfigFile.getNamespace());

  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return someAppId;
    }

    @Override
    public Env getApolloEnv() {
      return someEnv;
    }
  }

}
