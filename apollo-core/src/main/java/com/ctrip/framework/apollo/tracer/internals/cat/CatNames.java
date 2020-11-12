package com.ctrip.framework.apollo.tracer.internals.cat;

/**
 * Cat类、Cat事务类相关方法名及全限定名(大众点评 CAT系统监控)
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface CatNames {

  /**
   * Cat类全限定名称(大众点评 CAT系统监控)
   * <p>
   * 使用示例：https://vimsky.com/zh-tw/examples/detail/java-class-com.dianping.cat.Cat.html
   */
  String CAT_CLASS = "com.dianping.cat.Cat";
  /**
   * 记录错误方法名
   */
  String LOG_ERROR_METHOD = "logError";
  /**
   * 记录事件方法名
   */
  String LOG_EVENT_METHOD = "logEvent";
  /**
   * 创建新事务方法名
   */
  String NEW_TRANSACTION_METHOD = "newTransaction";
  /**
   * 事务接口全限定名称
   */
  String CAT_TRANSACTION_CLASS = "com.dianping.cat.message.Transaction";
  /**
   * 设置消息状态方法名
   */
  String SET_STATUS_METHOD = "setStatus";
  /**
   * 添加数据方法名
   */
  String ADD_DATA_METHOD = "addData";
  /**
   * 完成构造消息方法名
   */
  String COMPLETE_METHOD = "complete";
}
