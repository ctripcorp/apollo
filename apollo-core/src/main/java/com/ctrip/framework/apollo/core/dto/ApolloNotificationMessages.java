package com.ctrip.framework.apollo.core.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * apollo通知消息集
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@AllArgsConstructor
public class ApolloNotificationMessages {

  /**
   * 通知消息明细<key,通知消息id>
   */

  private Map<String, Long> details;

  public ApolloNotificationMessages() {
    this(Maps.<String, Long>newHashMap());
  }

  /**
   * 添加通知消息
   *
   * @param key            指定的key
   * @param notificationId 通知消息id
   */
  public void put(String key, long notificationId) {
    details.put(key, notificationId);
  }

  /**
   * 通过指定的key获取通知id
   *
   * @param key 指定的key
   * @return 指定key的通知id
   */
  public Long get(String key) {
    return this.details.get(key);
  }

  /**
   * 指定的key是否存在
   *
   * @param key 指定的key
   * @return true，存在，false,不存在
   */
  public boolean has(String key) {
    return this.details.containsKey(key);
  }

  /**
   * 是否为空
   *
   * @return true, 为空，否则，false
   */
  public boolean isEmpty() {
    return this.details.isEmpty();
  }

  /**
   * 将source合并
   *
   * @param source apollo通知消息
   */
  public void mergeFrom(ApolloNotificationMessages source) {
    if (source == null) {
      return;
    }

    for (Map.Entry<String, Long> entry : source.getDetails().entrySet()) {
      // 只合并新的通知编号大于的情况
      if (this.has(entry.getKey()) &&
          this.get(entry.getKey()) >= entry.getValue()) {
        continue;
      }
      this.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public ApolloNotificationMessages clone() {
    return new ApolloNotificationMessages(ImmutableMap.copyOf(this.details));
  }
}
