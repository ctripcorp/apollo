package com.ctrip.framework.apollo.biz.message;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;

/**
 * 发布消息监听接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseMessageListener {

  /**
   * 发布消息监听回调函数，用于处理新记录的发布回调
   *
   * @param message 发布消息
   * @param channel 通道名称
   */
  void handleMessage(ReleaseMessage message, String channel);
}
