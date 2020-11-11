package com.ctrip.framework.apollo.tracer.spi;

/**
 * Cat事务接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Transaction {

  /**
   * 成功的值
   */
  String SUCCESS = "0";

  /**
   * 设置消息状态
   *
   * @param status 消息状态,"0"表示成功,其它都是失败.
   */
  void setStatus(String status);

  /**
   * 使用Exception设置消息状态。
   *
   * @param e 异常.
   */
  void setStatus(Throwable e);

  /**
   * 添加一个键值对.
   *
   * @param key   键
   * @param value 值
   */
  void addData(String key, Object value);

  /**
   * 完成构造消息
   */
  void complete();
}
