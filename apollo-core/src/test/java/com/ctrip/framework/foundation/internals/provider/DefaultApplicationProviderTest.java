package com.ctrip.framework.foundation.internals.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import org.junit.Before;
import org.junit.Test;

public class DefaultApplicationProviderTest {
  private DefaultApplicationProvider defaultApplicationProvider;
  String PREDEFINED_APP_ID = "110402";

  @Before
  public void setUp() throws Exception {
    defaultApplicationProvider = new DefaultApplicationProvider();
  }

  @Test
  public void testLoadAppProperties() throws Exception {
    defaultApplicationProvider.initialize();

    assertEquals(PREDEFINED_APP_ID, defaultApplicationProvider.getAppId());
    assertTrue(defaultApplicationProvider.isAppIdSet());
  }

  @Test
  public void testLoadAppPropertiesWithUTF8Bom() throws Exception {
    File baseDir = new File("src/test/resources/META-INF");
    File appProperties = new File(baseDir, "app-with-utf8bom.properties");

    defaultApplicationProvider.initialize(new FileInputStream(appProperties));

    assertEquals(PREDEFINED_APP_ID, defaultApplicationProvider.getAppId());
    assertTrue(defaultApplicationProvider.isAppIdSet());
  }

  @Test
  public void testLoadAppPropertiesWithSystemProperty() throws Exception {
    String someAppId = "someAppId";
    String someSecret = "someSecret";
    System.setProperty("app.id", someAppId);
    System.setProperty("apollo.accesskey.secret", someSecret);
    defaultApplicationProvider.initialize();
    System.clearProperty("app.id");
    System.clearProperty("apollo.accesskey.secret");

    assertEquals(someAppId, defaultApplicationProvider.getAppId());
    assertTrue(defaultApplicationProvider.isAppIdSet());
    assertEquals(someSecret, defaultApplicationProvider.getAccessKeySecret());
  }

  @Test
  public void testLoadAppPropertiesFailed() throws Exception {
    File baseDir = new File("src/test/resources/META-INF");
    File appProperties = new File(baseDir, "some-invalid-app.properties");

    defaultApplicationProvider.initialize(new FileInputStream(appProperties));

    assertEquals(null, defaultApplicationProvider.getAppId());
    assertFalse(defaultApplicationProvider.isAppIdSet());
  }
}
