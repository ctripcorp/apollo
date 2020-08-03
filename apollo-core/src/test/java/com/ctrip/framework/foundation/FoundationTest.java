package com.ctrip.framework.foundation;

import static org.junit.Assert.assertTrue;

import com.ctrip.framework.foundation.internals.provider.DefaultApplicationProvider;
import com.ctrip.framework.foundation.internals.provider.DefaultServerProvider;
import org.junit.Assert;
import org.junit.Test;

public class FoundationTest {

   @Test
   public void testApp() {
      assertTrue(Foundation.app() instanceof DefaultApplicationProvider);
   }

   @Test
   public void testServer() {
      assertTrue(Foundation.server() instanceof DefaultServerProvider);
   }

   @Test
   public void testNet() {
      // 获取本机IP和HostName
      String hostAddress = Foundation.net().getHostAddress();
      String hostName = Foundation.net().getHostName();

      Assert.assertNotNull("No host address detected.", hostAddress);
      Assert.assertNotNull("No host name resolved.", hostName);
   }

}
