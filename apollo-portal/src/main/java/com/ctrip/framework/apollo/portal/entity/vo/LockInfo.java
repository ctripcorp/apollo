package com.ctrip.framework.apollo.portal.entity.vo;

import lombok.Data;

/**
 * 锁信息
 */
@Data
public class LockInfo {

  /**
   * 锁所有者
   */
  private String lockOwner;
  /**
   * 是否允许紧急发布
   */
  private boolean isEmergencyPublishAllowed;
}
