package com.ctrip.framework.apollo.tracer.spi;

/**
 * 消息生产者
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface MessageProducer {

  /**
   * 通过异常记录错误.
   *
   * @param cause 异常
   */
  void logError(Throwable cause);

  /**
   * 通过消息和异常记录错误.
   *
   * @param message 消息
   * @param cause   异常
   */
  void logError(String message, Throwable cause);

  /**
   * 通过事件类型和名称记录事件（Log an event in one shot with SUCCESS status.）
   *
   * @param type 事件类型
   * @param name 事件名称
   */
  void logEvent(String type, String name);

  /**
   * 通过事件类型、名称、状态、名称-值对的格式记录事件(Log an event in one shot.）
   *
   * @param type           事件类型
   * @param name           事件名称
   * @param status         "0"表示成功,其它为失败
   * @param nameValuePairs 名称-值对的格式为"a=1&b=2&..." w
   */
  void logEvent(String type, String name, String status, String nameValuePairs);

  /**
   * 创建具有给定类型和名称的新事务.
   *
   * @param type 事务类型
   * @param name 事务名称
   */
  Transaction newTransaction(String type, String name);
}
