package com.ctrip.framework.apollo.foundation;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ctrip.framework.foundation.Foundation;

public class FoundationTest {
  private static final String someEnv = "pro";

   @BeforeClass
   public static void before() {
      System.setProperty("env", someEnv);
   }

   @AfterClass
   public static void afterClass() {
      System.clearProperty("env");
   }

   @Test
   public void testApp() {
      // 获取AppId
      String appId = Foundation.app().getAppId();
      Assert.assertEquals("110402", appId);
   }

   @Test
   public void testServer() {
      // 获取当前环境
      String envType = Foundation.server().getEnvType();
      Assert.assertEquals(someEnv, envType);
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
