package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;

/**
 * 名称空间发布信息 Dto
 */
@Data
public class NamespaceReleaseDTO {

  /**
   * 发布标题
   */
  private String releaseTitle;
  /**
   * 发布说明
   */
  private String releaseComment;
  /**
   * 发布者
   */
  private String releasedBy;
  /**
   * 是否紧急发布
   */
  private Boolean isEmergencyPublish;
}