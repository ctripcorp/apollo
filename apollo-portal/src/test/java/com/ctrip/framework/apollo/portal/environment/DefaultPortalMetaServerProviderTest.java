package com.ctrip.framework.apollo.portal.environment;

import static org.junit.Assert.assertEquals;

import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import org.junit.After;
import org.junit.Test;

public class DefaultPortalMetaServerProviderTest extends AbstractUnitTest {

  private final DefaultPortalMetaServerProvider defaultPortalMetaServerProvider = DefaultPortalMetaServerProvider.getInstance();


  @After
  public void tearDown() throws Exception {
    System.clearProperty("dev_meta");
    System.clearProperty("fat_meta");
    defaultPortalMetaServerProvider.reload();
  }

  @Test
  public void testFromPropertyFile() {
    assertEquals("http://localhost:8080", defaultPortalMetaServerProvider.getMetaServerAddress(Env.LOCAL));
    assertEquals("${dev_meta}", defaultPortalMetaServerProvider.getMetaServerAddress(Env.DEV));
    assertEquals("${pro_meta}", defaultPortalMetaServerProvider.getMetaServerAddress(Env.PRO));
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
    defaultPortalMetaServerProvider.reload();
    assertEquals(someDevMetaAddress, defaultPortalMetaServerProvider.getMetaServerAddress(Env.DEV));
    assertEquals(someFatMetaAddress, defaultPortalMetaServerProvider.getMetaServerAddress(Env.FAT));

    String randomAddress = "randomAddress";
    String randomEnvironment = "randomEnvironment";
    System.setProperty(randomEnvironment + "_meta", randomAddress);
    // reload above added
    defaultPortalMetaServerProvider.reload();
    assertEquals(randomAddress,
        defaultPortalMetaServerProvider.getMetaServerAddress(Env.valueOf(randomEnvironment)));

    // clear the property
    System.clearProperty(randomEnvironment + "_meta");
  }

}