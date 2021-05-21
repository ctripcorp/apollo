package com.ctrip.framework.apollo.core.utils;

import com.ctrip.framework.test.tools.AloneRunner;
import com.ctrip.framework.test.tools.AloneWith;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/21
 */
@RunWith(AloneRunner.class)
@AloneWith(JUnit4.class)
public class DeferredLoggerStateTest {

  @Test
  public void testDeferredState() {
    Assert.assertFalse(DeferredLogger.isEnabled());

    DeferredLogger.enable();
    Assert.assertTrue(DeferredLogger.isEnabled());

    DeferredLogger.replayTo();
    Assert.assertFalse(DeferredLogger.isEnabled());

    DeferredLogger.enable();
    Assert.assertFalse(DeferredLogger.isEnabled());
  }

}
