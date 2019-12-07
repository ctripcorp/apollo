package com.ctrip.framework.apollo.tracer.internals;

import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class NullMessageProducerTest {
  private MessageProducer messageProducer;

  @Before
  public void setUp() throws Exception {
    messageProducer = new NullMessageProducer();
  }

  @Test
  public void testNewTransaction() throws Exception {
    String someType = "someType";
    String someName = "someName";
    assertTrue(messageProducer.newTransaction(someType, someName) instanceof NullTransaction);
  }

}