package com.ctrip.framework.apollo.spring.processor;

import java.util.List;

import com.ctrip.framework.apollo.spring.property.ValueMappingProperty;

/**
 * An abstract value mapping collector for a kind of property key
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public abstract class AbstractValueMappingCollector implements ValueMappingCollector {

  @Override
  public void postFilter(List<ValueMappingProperty> propList) {
    // do nothing
  }

}
