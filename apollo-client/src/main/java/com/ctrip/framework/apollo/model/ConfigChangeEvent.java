package com.ctrip.framework.apollo.model;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 命名空间配置更改时的更改事件.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@AllArgsConstructor
public class ConfigChangeEvent {

  /**
   * 名称空间
   */
  @Getter
  private final String namespace;
  /**
   * 更改记录Map<namespace，配置更改的记录>
   */
  private final Map<String, ConfigChange> changes;

  /**
   * 获取改变记录的key列表（即名称空间）
   *
   * @return t改变记录的key列表（即名称空间）
   */
  public Set<String> changedKeys() {
    return changes.keySet();
  }

  /**
   * 获取指定键的特定更改实例.
   *
   * @param key 指定键
   * @return 特定更改实例
   */
  public ConfigChange getChange(String key) {
    return changes.get(key);
  }

  /**
   * 检查指定的key是否存在记录
   *
   * @param key 指定的key
   * @return 如果指定key更改，则为true，否则为false.
   */
  public boolean isChanged(String key) {
    return changes.containsKey(key);
  }


}
