package com.ctrip.framework.apollo.core.utils;

import com.ctrip.framework.apollo.BaseIntegrationTest;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by gl49 on 2018/6/8.
 */
public class NetUtilTest extends BaseIntegrationTest {

  @Test
  public void testPingUrlWithStatusCode200() throws Exception {
    String someResponse = "some response";
    ContextHandler handler = mockServerHandler(HttpServletResponse.SC_OK, someResponse);
    startServerWithHandlers(handler);

    assertTrue(NetUtil.pingUrl("http://localhost:" + PORT));
  }

  @Test
  public void testPingUrlWithStatusCode404() throws Exception {
    String someResponse = "some response";
    startServerWithHandlers(mockServerHandler(HttpServletResponse.SC_NOT_FOUND, someResponse));

    assertFalse(NetUtil.pingUrl("http://localhost:" + PORT));
  }

  @Test
  public void testPingUrlWithServerNotStarted() throws Exception {
    assertFalse(NetUtil.pingUrl("http://localhost:" + PORT));
  }
}
