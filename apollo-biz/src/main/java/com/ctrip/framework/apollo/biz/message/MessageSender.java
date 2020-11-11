package com.ctrip.framework.apollo.biz.message;

/**
 * 消息发送器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface MessageSender {

  /**
   * 发送消息
   *
   * @param message 消息
   * @param channel 通道
   */
  void sendMessage(String message, String channel);
}
