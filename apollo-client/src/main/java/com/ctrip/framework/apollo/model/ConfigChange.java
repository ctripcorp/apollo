package com.ctrip.framework.apollo.model;


import com.ctrip.framework.apollo.enums.PropertyChangeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 更改配置信息
 * <p>保存配置更改的信息</p>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@AllArgsConstructor
public class ConfigChange {

  /**
   * 名称空间
   */
  private final String namespace;
  /**
   * 属性名称
   */
  private final String propertyName;
  /**
   * 旧值
   */
  private String oldValue;
  /**
   * 新值
   */
  private String newValue;
  /**
   * 改变的属性类型
   */
  private PropertyChangeType changeType;


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ConfigChange{");
    sb.append("namespace='").append(namespace).append('\'');
    sb.append(", propertyName='").append(propertyName).append('\'');
    sb.append(", oldValue='").append(oldValue).append('\'');
    sb.append(", newValue='").append(newValue).append('\'');
    sb.append(", changeType=").append(changeType);
    sb.append('}');
    return sb.toString();
  }
}
