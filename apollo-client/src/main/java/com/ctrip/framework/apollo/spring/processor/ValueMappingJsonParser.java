package com.ctrip.framework.apollo.spring.processor;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;
import com.google.gson.Gson;

/**
 * The value mapping parser for the property value of JSON
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingJsonParser extends AbstractValueMappingParser {

  // Thread safe
  private static final Gson GSON = new Gson();

  protected Object doParse(ValueMappingOriginValue value) {
    Object origValue = value.getOriginValue();
    Object destValue;
    String valStr = null;
    if (origValue instanceof String) {
      valStr = (String) origValue;
    } else {
      valStr = GSON.toJson(origValue);
    }
    if (StringUtils.isBlank(valStr)) {
      return null;
    }
    destValue = GSON.fromJson(valStr, value.getGenericType());
    return destValue;
  }
}
