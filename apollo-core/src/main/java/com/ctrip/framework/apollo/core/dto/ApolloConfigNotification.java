package com.ctrip.framework.apollo.core.dto;

import lombok.Data;

/**
 * apollo配置通知 dto
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class ApolloConfigNotification {

  /**
   * 名称空间名称
   */

  private String namespaceName;
  /**
   * 通知id
   */

  private long notificationId;
  /**
   * apollo通知消息集
   */
  private volatile ApolloNotificationMessages messages;

  public ApolloConfigNotification(String namespaceName, long notificationId) {
    this.namespaceName = namespaceName;
    this.notificationId = notificationId;
  }

  /**
   * 添加通知消息
   *
   * @param key            指定的key
   * @param notificationId 通知消息id
   */
  public void addMessage(String key, long notificationId) {
    // 创建 ApolloNotificationMessages 对象
    if (this.messages == null) {
      synchronized (this) {
        if (this.messages == null) {
          this.messages = new ApolloNotificationMessages();
        }
      }
    }
    // 添加到 `messages` 中
    this.messages.put(key, notificationId);
  }

  @Override
  public String toString() {
    return "ApolloConfigNotification{" +
        "namespaceName='" + namespaceName + '\'' +
        ", notificationId=" + notificationId +
        '}';
  }
}
