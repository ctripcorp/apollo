package com.ctrip.framework.apollo.portal.entity.model;

/**
 * 验证接口
 */
public interface Verifiable {

  /**
   * 是否错误
   *
   * @return true, 错误，否则，false
   */
  boolean isInvalid();

}
