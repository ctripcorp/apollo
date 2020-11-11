package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;

/**
 * 开放的名称空间编辑锁
 */
@Data
public class OpenNamespaceLockDTO {

  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 是否已经加锁
   */
  private Boolean isLocked;
  /**
   * 加锁者
   */
  private String lockedBy;

  @Override
  public String toString() {
    return "OpenNamespaceLockDTO{" +
        "namespaceName='" + namespaceName + '\'' +
        ", isLocked=" + isLocked +
        ", lockedBy='" + lockedBy + '\'' +
        '}';
  }
}
