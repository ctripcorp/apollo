package com.ctrip.framework.apollo.common.dto;

import com.ctrip.framework.apollo.common.utils.InputValidator;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AppDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * 应用名
   */
  private String name;
  /**
   * 应用Id
   */
  @Pattern(
      regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
      message = "Invalid AppId format: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
  )
  private String appId;
  /**
   * 部门Id
   */
  private String orgId;
  /**
   * 部门名字
   */
  private String orgName;
  /**
   * 所有者的名称
   */
  private String ownerName;
  /**
   * 所有者的邮箱
   */
  private String ownerEmail;
}
