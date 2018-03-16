package com.ctrip.framework.apollo.spring.processor;

import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;

/**
 * An abstract value mapping parser for a property value
 *
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public abstract class AbstractValueMappingParser implements ValueMappingParser {

  @Override
  public Object parse(ValueMappingOriginValue value) {
    Object origValue = value.getOriginValue();
    Object destValue;
    if (origValue == null || value.getType().isInstance(origValue)) {
      destValue = origValue;
    } else {
      destValue = doParse(value);
    }
    return destValue;
  }

  /**
   * Parse the original property value as a different and specific type
   * 
   * @param value The original property value and type info
   * @return The actual value with the declared type
   */
  protected abstract Object doParse(ValueMappingOriginValue value);

}
