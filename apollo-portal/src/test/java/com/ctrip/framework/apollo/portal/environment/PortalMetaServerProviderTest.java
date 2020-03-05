package com.ctrip.framework.apollo.portal.environment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PortalMetaServerProviderTest {

  private PortalMetaServerProvider portalMetaServerProvider;

  @Before
  public void setUp() throws Exception {
    portalMetaServerProvider = PortalMetaServerProvider.getInstance();
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("dev_meta");
    System.clearProperty("fat_meta");
    PortalMetaServerProvider.getInstance().reset();
  }

  @Test
  public void testFromPropertyFile() {
    assertEquals("http://localhost:8080", portalMetaServerProvider.getMetaServerAddress(Env.LOCAL));
    assertEquals("${dev_meta}", portalMetaServerProvider.getMetaServerAddress(Env.DEV));
    assertEquals("${pro_meta}", portalMetaServerProvider.getMetaServerAddress(Env.PRO));
  }

  /**
   * testing the environment dynamic added from system property
   */
  @Test
  public void testDynamicEnvironmentFromSystemProperty() {
    String someDevMetaAddress = "someMetaAddress";
    String someFatMetaAddress = "someFatMetaAddress";
    System.setProperty("dev_meta", someDevMetaAddress);
    System.setProperty("fat_meta", someFatMetaAddress);
    // reload above added
    portalMetaServerProvider.reset();
    assertEquals(someDevMetaAddress, portalMetaServerProvider.getMetaServerAddress(Env.DEV));
    assertEquals(someFatMetaAddress, portalMetaServerProvider.getMetaServerAddress(Env.FAT));

    String randomAddress = "randomAddress";
    String randomEnvironment = "randomEnvironment";
    System.setProperty(randomEnvironment + "_meta", randomAddress);
    // reload above added
    portalMetaServerProvider.reset();
    assertEquals(randomAddress,
        portalMetaServerProvider.getMetaServerAddress(Env.valueOf(randomEnvironment)));

    // clear the property
    System.clearProperty(randomEnvironment + "_meta");
  }

}