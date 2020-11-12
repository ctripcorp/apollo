package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开发的应用名称空间 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenAppNamespaceDTO extends BaseDTO {

  /**
   * 名称空间名称
   */
  private String name;
  /**
   * 应用Id
   */
  private String appId;
  /**
   * namespace的格式（后缀）类型
   */
  private String format;
  /**
   * 名称空间是否为公共
   */
  private Boolean isPublic;

  /**
   * 是否为公共命名空间名称附加命名空间前缀
   */
  private Boolean appendNamespacePrefix = true;
  /**
   * 备注
   */
  private String comment;

  @Override
  public String toString() {
    return "OpenAppNamespaceDTO{" +
        "name='" + name + '\'' +
        ", appId='" + appId + '\'' +
        ", format='" + format + '\'' +
        ", isPublic=" + isPublic +
        ", appendNamespacePrefix=" + appendNamespacePrefix +
        ", comment='" + comment + '\'' +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
