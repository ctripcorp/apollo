package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 名称空间的编辑锁
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NamespaceLockDTO extends BaseDTO {

  /**
   * 集群NamespaceId
   */
  private Long namespaceId;
}
