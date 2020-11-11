package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用名称空间 dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AppNamespaceDTO extends BaseDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * namespace名字，注意，需要全局唯一
   */
  private String name;
  /**
   * appId
   */
  private String appId;
  /**
   * 备注
   */
  private String comment;
  /**
   * namespace的格式（后缀）类型
   */
  private String format;
  /**
   * namespace是否为公共
   */
  private Boolean isPublic = false;
}
