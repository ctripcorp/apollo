package com.ctrip.framework.apollo.spring.property;

import java.lang.reflect.Type;

import com.ctrip.framework.apollo.spring.processor.ValueMappingParser;

/**
 * The parameter class for {@link ValueMappingParser#parse(ValueMappingOriginValue)}
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingOriginValue {

  /**
   * The type of property value
   */
  private final Class<?> type;

  /**
   * The generic type of property value
   */
  private final Type genericType;

  /**
   * The original value
   */
  private final Object originValue;

  public ValueMappingOriginValue(Object originValue, Class<?> type, Type genericType) {
    if (type == null) {
      throw new IllegalArgumentException("type may not be null");
    }
    if (genericType == null) {
      throw new IllegalArgumentException("genericType may not be null");
    }

    this.type = type;
    this.genericType = genericType;
    this.originValue = originValue;
  }

  public Class<?> getType() {
    return type;
  }

  public Type getGenericType() {
    return genericType;
  }

  public Object getOriginValue() {
    return originValue;
  }

}
