package com.ctrip.framework.apollo.tracer;

import com.ctrip.framework.apollo.tracer.internals.NullMessageProducerManager;
import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import com.ctrip.framework.apollo.tracer.spi.MessageProducerManager;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import lombok.extern.slf4j.Slf4j;

/**
 * 跟踪器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public abstract class Tracer {

  /**
   * 空消息生产者管理器
   */
  private static final MessageProducerManager NULL_MESSAGE_PRODUCER_MANAGER =
      new NullMessageProducerManager();
  /**
   * 消息生产者管理器
   */
  private static volatile MessageProducerManager producerManager;
  /**
   * 对象锁
   */
  private static Object lock = new Object();

  /**
   * 静态代码块
   */
  static {
    getProducer();
  }

  /**
   * 获取消息生产者对象
   * <p>默认取{@code MessageProducerManager}</p>
   * <p>出现异常使用{@code NullMessageProducerManager}</p>
   *
   * @return 消息生产者对象
   */
  private static MessageProducer getProducer() {
    try {
      if (producerManager == null) {
        synchronized (lock) {
          if (producerManager == null) {
            producerManager = ServiceBootstrap.loadFirst(MessageProducerManager.class);
          }
        }
      }
    } catch (Throwable ex) {
      log.error(
          "Failed to initialize message producer manager, use null message producer manager.", ex);
      producerManager = NULL_MESSAGE_PRODUCER_MANAGER;
    }
    return producerManager.getProducer();
  }

  /**
   * 通过消息和异常记录错误.
   *
   * @param message 消息
   * @param cause   异常
   */
  public static void logError(String message, Throwable cause) {
    try {
      getProducer().logError(message, cause);
    } catch (Throwable ex) {
      log.warn("Failed to log error for message: {}, cause: {}", message, cause, ex);
    }
  }

  /**
   * 通过异常记录错误.
   *
   * @param cause 异常
   */
  public static void logError(Throwable cause) {
    try {
      getProducer().logError(cause);
    } catch (Throwable ex) {
      log.warn("Failed to log error for cause: {}", cause, ex);
    }
  }

  /**
   * 通过事件类型和名称记录事件（Log an event in one shot with SUCCESS status.）
   *
   * @param type 事件类型
   * @param name 事件名称
   */
  public static void logEvent(String type, String name) {
    try {
      getProducer().logEvent(type, name);
    } catch (Throwable ex) {
      log.warn("Failed to log event for type: {}, name: {}", type, name, ex);
    }
  }

  /**
   * 通过事件类型、名称、状态、名称-值对的格式记录事件(Log an event in one shot.）
   *
   * @param type           事件类型
   * @param name           事件名称
   * @param status         "0"表示成功,其它为失败
   * @param nameValuePairs 名称-值对的格式为"a=1&b=2&..." w
   */
  public static void logEvent(String type, String name, String status, String nameValuePairs) {
    try {
      getProducer().logEvent(type, name, status, nameValuePairs);
    } catch (Throwable ex) {
      log.warn("Failed to log event for type: {}, name: {}, status: {}, nameValuePairs: {}",
          type, name, status, nameValuePairs, ex);
    }
  }

  /**
   * 创建具有给定类型和名称的新事务.
   *
   * @param type 事务类型
   * @param name 事务名称
   */
  public static Transaction newTransaction(String type, String name) {
    try {
      return getProducer().newTransaction(type, name);
    } catch (Throwable ex) {
      log.warn("Failed to create transaction for type: {}, name: {}", type, name, ex);
      return NULL_MESSAGE_PRODUCER_MANAGER.getProducer().newTransaction(type, name);
    }
  }
}
