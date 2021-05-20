package com.ctrip.framework.apollo.core.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/20
 */
public class DeferredLogCacheTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final String logMsg = "hello kl";

  @Test
  public void testDeferredLogCacheMaxLogSize() {
    for (int i = 0; i < 20000; i++) {
      DeferredLogCache.info(logger, "DeferredLogUtilTest");
    }
    Assert.assertEquals(DeferredLogCache.logSize(), DeferredLogCache.MAX_LOG_SIZE);
  }

  @Test
  public void testEnableDeferredLog() {
    Assert.assertFalse(DeferredLogCache.isEnabled());

    DeferredLogCache.enableDeferredLog();
    Assert.assertTrue(DeferredLogCache.isEnabled());

    DeferredLogCache.replayTo();
    Assert.assertFalse(DeferredLogCache.isEnabled());

    final Logger defaultLogger = DeferredLoggerFactory.getLogger(DeferredLoggerTest.class);
    defaultLogger.info(logMsg);
    defaultLogger.debug(logMsg);
    defaultLogger.warn(logMsg);
    Assert.assertEquals(0, DeferredLogCache.logSize());

    DeferredLogCache.enableDeferredLog();
    defaultLogger.info(logMsg);
    defaultLogger.debug(logMsg);
    defaultLogger.warn(logMsg);
    defaultLogger.error(logMsg, new RuntimeException());
    Assert.assertEquals(4, DeferredLogCache.logSize());
  }
}
