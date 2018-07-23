package com.ctrip.framework.apollo.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ctrip.framework.apollo.BaseIntegrationTest;
import com.ctrip.framework.apollo.core.enums.Env;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Test;

public class MetaDomainTest extends BaseIntegrationTest {

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    System.clearProperty("fat_meta");
    System.clearProperty("uat_meta");
    System.clearProperty("lpt_meta");
    System.clearProperty("apollo.meta");
  }

  @Test
  public void testGetMetaDomain() {
    assertEquals("http://localhost:8080", MetaDomainConsts.getDomain(Env.LOCAL));
    assertEquals("http://dev:8080", MetaDomainConsts.getDomain(Env.DEV));
    assertEquals(MetaDomainConsts.DEFAULT_META_URL, MetaDomainConsts.getDomain(Env.PRO));
  }

  @Test
  public void testGetMetaDomainWithSystemProperty() throws Exception {
    String someMeta = "some-meta";
    Env someEnv = Env.DEV;

    System.setProperty("apollo.meta", someMeta);

    assertEquals(someMeta, MetaDomainConsts.getDomain(someEnv));
  }

  @Test
  public void testGetValidAddress() throws Exception {
    String someResponse = "some response";
    startServerWithHandlers(mockServerHandler(HttpServletResponse.SC_OK, someResponse));

    String validServer = " http://localhost:" + PORT + " ";
    String invalidServer = "http://localhost:" + findFreePort();

    System.setProperty("fat_meta", validServer + "," + invalidServer);
    System.setProperty("uat_meta", invalidServer + "," + validServer);

    MetaDomainConsts.initialize();

    assertEquals(validServer.trim(), MetaDomainConsts.getDomain(Env.FAT));
    assertEquals(validServer.trim(), MetaDomainConsts.getDomain(Env.UAT));
  }

  @Test
  public void testInvalidAddress() throws Exception {
    String invalidServer = "http://localhost:" + findFreePort() + " ";
    String anotherInvalidServer = "http://localhost:" + findFreePort() + " ";

    System.setProperty("lpt_meta", invalidServer + "," + anotherInvalidServer);

    MetaDomainConsts.initialize();

    String metaServer = MetaDomainConsts.getDomain(Env.LPT);

    assertTrue(metaServer.equals(invalidServer.trim()) || metaServer.equals(anotherInvalidServer.trim()));
  }
}
