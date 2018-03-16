package com.ctrip.framework.apollo.spring.property;

import java.lang.reflect.Type;

import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.processor.ValueMappingCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingParser;

/**
 * The definition holder of {@link ValueMapping}
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingHolder {

  /**
   * The placeholder expression like "${timeout:100}"
   */
  private final String placeholder;

  /**
   * The property key expression like "timeout"
   */
  private final String propKey;

  /**
   * The property default value like "100"
   */
  private final String defaultValue;

  /**
   * Type of property value
   */
  private final Class<?> type;

  /**
   * The generic type of property value
   */
  private final Type genericType;

  /**
   * Value mapping parser
   */
  private final ValueMappingParser parser;

  /**
   * Value mapping collector, may be null
   */
  private final ValueMappingCollector collector;

  public ValueMappingHolder(String placeholder, String propKey, String defaultValue, Class<?> type,
      Type genericType, ValueMappingParser parser, ValueMappingCollector collector) {
    if (placeholder == null) {
      throw new IllegalArgumentException("placeholder may not be null");
    }
    if (propKey == null) {
      throw new IllegalArgumentException("propKey may not be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("type may not be null");
    }
    if (genericType == null) {
      throw new IllegalArgumentException("genericType may not be null");
    }
    if (collector != null && parser == null) {
      throw new IllegalArgumentException("collector is valid, parser may not be null");
    }
    this.placeholder = placeholder;
    this.propKey = propKey;
    this.defaultValue = defaultValue;
    this.type = type;
    this.genericType = genericType;
    this.parser = parser;
    this.collector = collector;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public String getPropKey() {
    return propKey;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Class<?> getType() {
    return type;
  }

  public Type getGenericType() {
    return genericType;
  }

  public ValueMappingParser getParser() {
    return parser;
  }

  public ValueMappingCollector getCollector() {
    return collector;
  }

  public boolean isValueMapping() {
    return parser != null;
  }

  public boolean hasCollector() {
    return collector != null;
  }

}
