package com.ctrip.framework.apollo.common.config;

import java.util.Map;
import org.springframework.core.env.MapPropertySource;

/**
 * 属性源刷新功能
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class RefreshablePropertySource extends MapPropertySource {

  /**
   * 构造属性源刷新对象
   *
   * @param name   名称
   * @param source 数据源
   */
  public RefreshablePropertySource(String name, Map<String, Object> source) {
    super(name, source);
  }

  @Override
  public Object getProperty(String name) {
    return this.source.get(name);
  }

  /**
   * 刷新属性
   */
  protected abstract void refresh();

}
