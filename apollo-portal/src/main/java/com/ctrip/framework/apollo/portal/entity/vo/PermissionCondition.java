package com.ctrip.framework.apollo.portal.entity.vo;

import lombok.Data;

/**
 * 权限状态实体
 */
@Data
public class PermissionCondition {

  /**
   * 是否有权限.
   */
  private boolean hasPermission;
}
