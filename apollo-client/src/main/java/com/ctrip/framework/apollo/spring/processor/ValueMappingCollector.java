package com.ctrip.framework.apollo.spring.processor;

import java.util.List;

import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.property.ValueMappingProperty;

/**
 * The collector interface for {@link ValueMapping#collector()}, is used to collect a kind of
 * properties and convert them to the value of Collection or Array.
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public interface ValueMappingCollector {

  /**
   * Filter the target property，to judge a property if can be collected
   * 
   * @param localKey Local property key
   * @param remoteKey Remote property key
   * @param propValue Remote property value
   * @return boolean true: collectible property，false: uncollectible property
   */
  boolean filter(String localKey, String remoteKey, String propValue);

  /**
   * The post process method after filtering, may be used to sort the filtered property list, or
   * remove some invalid property
   * 
   * @param propList The filtered property list
   * @return
   */
  void postFilter(List<ValueMappingProperty> propList);
}
