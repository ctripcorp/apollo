package com.ctrip.framework.apollo.tracer.spi;

/**
 * 消息生产者管理器接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface MessageProducerManager {

  /**
   * 获取消息生产者
   *
   * @return 消息生产者
   */
  MessageProducer getProducer();
}
