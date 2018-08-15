package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import org.junit.After;
import org.junit.Test;

public class DefaultMetaServerProviderTest {

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ConfigConsts.APOLLO_META_KEY);
  }

  @Test
  public void testWithSystemProperty() throws Exception {
    String someMetaAddress = "someMetaAddress";
    Env someEnv = Env.DEV;

    System.setProperty(ConfigConsts.APOLLO_META_KEY, " " + someMetaAddress + " ");

    DefaultMetaServerProvider defaultMetaServerProvider = new DefaultMetaServerProvider();

    assertEquals(someMetaAddress, defaultMetaServerProvider.getMetaServerAddress(someEnv));
  }

  @Test
  public void testWithNoSystemProperty() throws Exception {
    Env someEnv = Env.DEV;

    DefaultMetaServerProvider defaultMetaServerProvider = new DefaultMetaServerProvider();

    assertNull(defaultMetaServerProvider.getMetaServerAddress(someEnv));
  }
}
