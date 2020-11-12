package com.ctrip.framework.apollo.model;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置文件更改事件 Model
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Getter
@AllArgsConstructor
public class ConfigFileChangeEvent {

  /**
   * 名称空间
   */
  private final String namespace;
  /**
   * 旧值
   */
  private final String oldValue;
  /**
   * 新值
   */
  private final String newValue;
  /**
   * 改变的属性类型
   */
  private final PropertyChangeType changeType;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ConfigFileChangeEvent{");
    sb.append("namespace='").append(namespace).append('\'');
    sb.append(", oldValue='").append(oldValue).append('\'');
    sb.append(", newValue='").append(newValue).append('\'');
    sb.append(", changeType=").append(changeType);
    sb.append('}');
    return sb.toString();
  }
}
