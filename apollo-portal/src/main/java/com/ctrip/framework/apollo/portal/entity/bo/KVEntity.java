package com.ctrip.framework.apollo.portal.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 健值实体
 */
@AllArgsConstructor
@Data
public class KVEntity {

  /**
   * 健
   */
  private String key;
  /**
   * 值
   */
  private String value;
}
