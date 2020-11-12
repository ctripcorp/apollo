package com.ctrip.framework.apollo.spring.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring Value定义
 */
@AllArgsConstructor
@Getter
public class SpringValueDefinition {

  /**
   * Key 即在Config中的key
   */
  private final String key;
  /**
   * 占位符
   */
  private final String placeholder;
  /**
   * 属性名
   */
  private final String propertyName;
}
