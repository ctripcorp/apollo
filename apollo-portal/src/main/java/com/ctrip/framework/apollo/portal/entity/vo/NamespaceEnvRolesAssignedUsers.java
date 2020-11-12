package com.ctrip.framework.apollo.portal.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 名称空间环境分配的角色用户
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NamespaceEnvRolesAssignedUsers extends NamespaceRolesAssignedUsers {

  /**
   * 环境
   */
  private String env;
}
