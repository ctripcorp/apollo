package com.ctrip.framework.apollo.foundation.internals.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ctrip.framework.foundation.internals.provider.DefaultServerProvider;

public class DefaultServerProviderTest {
  private DefaultServerProvider defaultServerProvider;

  @Before
  public void setUp() throws Exception {
    cleanUp();
    defaultServerProvider = new DefaultServerProvider();
  }

  @After
  public void tearDown() throws Exception {
    cleanUp();
  }

  private void cleanUp() {
    System.clearProperty("env");
    System.clearProperty("idc");
  }

  @Test
  public void testEnvWithSystemProperty() throws Exception {
    String someEnv = "someEnv";
    String someDc = "someDc";
    System.setProperty("env", someEnv);
    System.setProperty("idc", someDc);

    defaultServerProvider.initialize(null);

    assertEquals(someEnv, defaultServerProvider.getEnvType());
    assertEquals(someDc, defaultServerProvider.getDataCenter());
  }

  @Test
  public void testWithPropertiesStream() throws Exception {
    File baseDir = new File("src/test/resources/properties");
    File serverProperties = new File(baseDir, "server.properties");
    defaultServerProvider.initialize(new FileInputStream(serverProperties));

    assertEquals("SHAJQ", defaultServerProvider.getDataCenter());
    assertTrue(defaultServerProvider.isEnvTypeSet());
    assertEquals("DEV", defaultServerProvider.getEnvType());
  }

  @Test
  public void testWithUTF8BomPropertiesStream() throws Exception {
    File baseDir = new File("src/test/resources/properties");
    File serverProperties = new File(baseDir, "server-with-utf8bom.properties");
    defaultServerProvider.initialize(new FileInputStream(serverProperties));

    assertEquals("SHAJQ", defaultServerProvider.getDataCenter());
    assertTrue(defaultServerProvider.isEnvTypeSet());
    assertEquals("DEV", defaultServerProvider.getEnvType());
  }

  @Test
  public void testWithPropertiesStreamAndEnvFromSystemProperty() throws Exception {
    String prodEnv = "pro";
    System.setProperty("env", prodEnv);

    File baseDir = new File("src/test/resources/properties");
    File serverProperties = new File(baseDir, "server.properties");
    defaultServerProvider.initialize(new FileInputStream(serverProperties));

    String predefinedDataCenter = "SHAJQ";

    assertEquals(predefinedDataCenter, defaultServerProvider.getDataCenter());
    assertTrue(defaultServerProvider.isEnvTypeSet());
    assertEquals(prodEnv, defaultServerProvider.getEnvType());
  }

  @Test
  public void testWithNoPropertiesStream() throws Exception {
    defaultServerProvider.initialize(null);

    assertNull(defaultServerProvider.getDataCenter());
    assertFalse(defaultServerProvider.isEnvTypeSet());
    assertNull(defaultServerProvider.getEnvType());
  }
}
