package com.ctrip.framework.apollo.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/11
 */
public class DeferredLoggerTest {

  private static ByteArrayOutputStream outContent;
  private static Logger logger = null;
  private static PrintStream printStream;

  @BeforeClass
  public static void init() {
    DeferredLoggerTest.outContent = new ByteArrayOutputStream();
    DeferredLoggerTest.printStream = new PrintStream(DeferredLoggerTest.outContent);
    System.setOut(DeferredLoggerTest.printStream);
    DeferredLoggerTest.logger = DeferredLoggerFactory.getLogger("DeferredLoggerTest");
  }

  @Test
  public void testErrorLog() {
    DeferredLoggerTest.logger.error("errorLogger");
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("errorLogger"));
  }

  @Test
  public void testInfoLog() {
    DeferredLoggerTest.logger.info("inFoLogger");
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("inFoLogger"));
  }

  @Test
  public void testWarnLog() {
    DeferredLoggerTest.logger.warn("warnLogger");
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("warnLogger"));
  }

  @Test
  public void testDebugLog() {
    DeferredLoggerTest.logger.warn("debugLogger");
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("debugLogger"));
  }

  @Test
  public void testDeferredLog() {
    DeferredLogCache.enableDeferredLog();

    DeferredLoggerTest.logger.error("errorLogger");
    DeferredLoggerTest.logger.info("inFoLogger");
    DeferredLoggerTest.logger.warn("warnLogger");
    DeferredLoggerTest.logger.debug("debugLogger");
    DeferredLogCache.replayTo();

    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("errorLogger"));
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("inFoLogger"));
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("warnLogger"));
    Assert.assertTrue(DeferredLoggerTest.outContent.toString().contains("debugLogger"));

  }

}
