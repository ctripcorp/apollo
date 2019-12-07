package com.ctrip.framework.apollo.core;

import com.ctrip.framework.apollo.BaseIntegrationTest;
import com.ctrip.framework.apollo.core.constants.Env;
import com.ctrip.framework.apollo.core.internals.LegacyMetaServerProvider;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetaDomainTest extends BaseIntegrationTest {

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    MockMetaServerProvider.clear();
  }

  @Test
  public void testGetMetaDomain() {
    assertEquals("http://localhost:8080", MetaDomainConsts.getDomain(Env.LOCAL));
    assertEquals("http://dev:8080", MetaDomainConsts.getDomain(Env.DEV));
    assertEquals(MetaDomainConsts.DEFAULT_META_URL, MetaDomainConsts.getDomain(Env.PRO));
  }

  @Test
  public void testGetValidAddress() throws Exception {
    String someResponse = "some response";
    startServerWithHandlers(mockServerHandler(HttpServletResponse.SC_OK, someResponse));

    String validServer = " http://localhost:" + PORT + " ";
    String invalidServer = "http://localhost:" + findFreePort();

    MockMetaServerProvider.mock(Env.FAT, validServer + "," + invalidServer);
    MockMetaServerProvider.mock(Env.UAT, invalidServer + "," + validServer);

    assertEquals(validServer.trim(), MetaDomainConsts.getDomain(Env.FAT));
    assertEquals(validServer.trim(), MetaDomainConsts.getDomain(Env.UAT));
  }

  @Test
  public void testInvalidAddress() throws Exception {
    String invalidServer = "http://localhost:" + findFreePort() + " ";
    String anotherInvalidServer = "http://localhost:" + findFreePort() + " ";

    MockMetaServerProvider.mock(Env.LPT, invalidServer + "," + anotherInvalidServer);

    String metaServer = MetaDomainConsts.getDomain(Env.LPT);

    assertTrue(metaServer.equals(invalidServer.trim()) || metaServer.equals(anotherInvalidServer.trim()));
  }

  public static class MockMetaServerProvider implements MetaServerProvider {

    private static Map<String, String> mockMetaServerAddress = Maps.newHashMap();

    private static void mock(String env, String metaServerAddress) {
      mockMetaServerAddress.put(env, metaServerAddress);
    }

    private static void clear() {
      mockMetaServerAddress.clear();
    }

    @Override
    public String getMetaServerAddress(String targetEnv) {
      return mockMetaServerAddress.get(targetEnv);
    }

    @Override
    public int getOrder() {
      return LegacyMetaServerProvider.ORDER - 1;// just in front of LegacyMetaServerProvider
    }
  }
}
