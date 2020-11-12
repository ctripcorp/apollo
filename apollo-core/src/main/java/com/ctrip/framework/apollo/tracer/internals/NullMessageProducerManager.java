package com.ctrip.framework.apollo.tracer.internals;

import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import com.ctrip.framework.apollo.tracer.spi.MessageProducerManager;

/**
 * 空消息生产者管理器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class NullMessageProducerManager implements MessageProducerManager {

  /**
   * 空消息生产者
   */
  private static final MessageProducer producer = new NullMessageProducer();

  @Override
  public MessageProducer getProducer() {
    return producer;
  }
}
