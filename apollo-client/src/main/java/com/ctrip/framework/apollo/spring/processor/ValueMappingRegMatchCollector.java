package com.ctrip.framework.apollo.spring.processor;

/**
 * The regular expression matching collector for a kind of property key
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingRegMatchCollector extends AbstractValueMappingCollector {

  @Override
  public boolean filter(String localKeyRegex, String remoteKey, String propValue) {
    return propValue != null && remoteKey.matches(localKeyRegex);
  }

}
