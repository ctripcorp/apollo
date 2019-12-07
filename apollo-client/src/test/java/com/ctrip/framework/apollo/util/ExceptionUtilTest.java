package com.ctrip.framework.apollo.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ExceptionUtilTest {

  @Test
  public void testGetDetailMessageWithNoCause() throws Exception {
    String someMessage = "some message";
    Throwable ex = new Throwable(someMessage);
    assertEquals(someMessage, ExceptionUtil.getDetailMessage(ex));
  }

  @Test
  public void testGetDetailMessageWithCauses() throws Exception {
    String causeMsg1 = "some cause";
    String causeMsg2 = "another cause";
    String someMessage = "some message";

    Throwable cause2 = new Throwable(causeMsg2);
    Throwable cause1 = new Throwable(causeMsg1, cause2);
    Throwable ex = new Throwable(someMessage, cause1);

    String expected = someMessage + " [Cause: " + causeMsg1 + " [Cause: " + causeMsg2 + "]]";
    assertEquals(expected, ExceptionUtil.getDetailMessage(ex));
  }

  @Test
  public void testGetDetailMessageWithCauseMessageNull() throws Exception {
    String someMessage = "some message";
    Throwable cause = new Throwable();
    Throwable ex = new Throwable(someMessage, cause);

    assertEquals(someMessage, ExceptionUtil.getDetailMessage(ex));
  }

  @Test
  public void testGetDetailMessageWithNullMessage() throws Exception {
    Throwable ex = new Throwable();

    assertEquals("", ExceptionUtil.getDetailMessage(ex));
    assertEquals("", ExceptionUtil.getDetailMessage(null));

  }
}
