package com.ctrip.framework.apollo.spring.processor;

import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;

/**
 * The parser interface for {@link ValueMapping#parser()}, is used to convert property value to the
 * type of the annotated argument
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public interface ValueMappingParser {

  /**
   * Parse the original property value as a specific type
   * 
   * @param value The original property value and type info
   * @return The actual value with the declared type
   */
  Object parse(ValueMappingOriginValue value);
}
