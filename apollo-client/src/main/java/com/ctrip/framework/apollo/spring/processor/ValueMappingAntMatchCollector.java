package com.ctrip.framework.apollo.spring.processor;

import org.springframework.util.AntPathMatcher;

/**
 * The ant path matching collector for a kind of property key
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingAntMatchCollector extends AbstractValueMappingCollector {

  /**
   * Ant matcher
   */
  private static final AntPathMatcher MATCHER = new AntPathMatcher();

  @Override
  public boolean filter(String localKeyPattern, String remoteKey, String propValue) {
    return propValue != null && MATCHER.match(localKeyPattern, remoteKey);
  }

}
