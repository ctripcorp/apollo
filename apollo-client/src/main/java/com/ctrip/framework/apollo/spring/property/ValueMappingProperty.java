package com.ctrip.framework.apollo.spring.property;

/**
 * Value mapping property
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingProperty {

  /**
   * Remote property key
   */
  private final String key;

  /**
   * Local property value with the declared type
   */
  private final Object value;

  public ValueMappingProperty(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("key may not be null");
    }
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

}
