package com.ctrip.framework.apollo.tracer.internals.cat;

import com.ctrip.framework.apollo.tracer.spi.MessageProducer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import java.lang.reflect.Method;

/**
 * Cat消息生产者(通过反射来调用)
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class CatMessageProducer implements MessageProducer {

  /**
   * Cat的Class
   */
  private static Class CAT_CLASS;
  /**
   * 通过异常记录错误方法
   */
  private static Method LOG_ERROR_WITH_CAUSE;
  /**
   * 通过消息和异常记录错误方法
   */
  private static Method LOG_ERROR_WITH_MESSAGE_AND_CAUSE;
  /**
   * 通过事件类型和名称记录事件方法
   */
  private static Method LOG_EVENT_WITH_TYPE_AND_NAME;
  /**
   * 通过事件类型、名称、状态、名称-值对的格式记录事件方法
   */
  private static Method LOG_EVENT_WITH_TYPE_AND_NAME_AND_STATUS_AND_NAME_VALUE_PAIRS;
  /**
   * 创建具有给定类型和名称的新事务方法
   */
  private static Method NEW_TRANSACTION_WITH_TYPE_AND_NAME;

  /**
   * 静态代码块，静态属性初始化
   */
  static {
    try {
      CAT_CLASS = Class.forName(CatNames.CAT_CLASS);
      LOG_ERROR_WITH_CAUSE = CAT_CLASS.getMethod(CatNames.LOG_ERROR_METHOD, Throwable.class);
      LOG_ERROR_WITH_MESSAGE_AND_CAUSE = CAT_CLASS.getMethod(CatNames.LOG_ERROR_METHOD,
          String.class, Throwable.class);
      LOG_EVENT_WITH_TYPE_AND_NAME = CAT_CLASS.getMethod(CatNames.LOG_EVENT_METHOD,
          String.class, String.class);
      LOG_EVENT_WITH_TYPE_AND_NAME_AND_STATUS_AND_NAME_VALUE_PAIRS =
          CAT_CLASS.getMethod(CatNames.LOG_EVENT_METHOD, String.class, String.class,
              String.class, String.class);
      NEW_TRANSACTION_WITH_TYPE_AND_NAME = CAT_CLASS.getMethod(
          CatNames.NEW_TRANSACTION_METHOD, String.class, String.class);
      //eager init CatTransaction
      CatTransaction.init();
    } catch (Throwable ex) {
      throw new IllegalStateException("Initialize Cat message producer failed", ex);
    }
  }

  @Override
  public void logError(Throwable cause) {
    try {
      LOG_ERROR_WITH_CAUSE.invoke(null, cause);
    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public void logError(String message, Throwable cause) {
    try {
      LOG_ERROR_WITH_MESSAGE_AND_CAUSE.invoke(null, message, cause);
    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public void logEvent(String type, String name) {
    try {
      LOG_EVENT_WITH_TYPE_AND_NAME.invoke(null, type, name);
    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public void logEvent(String type, String name, String status, String nameValuePairs) {
    try {
      LOG_EVENT_WITH_TYPE_AND_NAME_AND_STATUS_AND_NAME_VALUE_PAIRS.invoke(null, type, name,
          status, nameValuePairs);
    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public Transaction newTransaction(String type, String name) {
    try {
      return new CatTransaction(NEW_TRANSACTION_WITH_TYPE_AND_NAME.invoke(null, type, name));
    } catch (Throwable ex) {
      throw new IllegalStateException(ex);
    }
  }
}
