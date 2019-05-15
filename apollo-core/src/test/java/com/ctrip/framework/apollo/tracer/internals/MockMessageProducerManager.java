package com.ctrip.framework.apollo.tracer.internals;

import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import com.ctrip.framework.apollo.tracer.spi.MessageProducerManager;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class MockMessageProducerManager implements MessageProducerManager {
  private static MessageProducer s_producer;

  @Override
  public MessageProducer getProducer() {
    return s_producer;
  }

  public static void setProducer(MessageProducer producer) {
    s_producer = producer;
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
