package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访问密钥 dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AccessKeyDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * 密钥
   */
  private String secret;
  /**
   * 应用id
   */
  private String appId;
  /**
   * 是否启用，默认禁用（1:启用 0:禁用）
   */
  private Boolean enabled;
}
