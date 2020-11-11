package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开放的属性配置项 Dto
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenItemDTO extends BaseDTO {

  /**
   * 配置项Key
   */
  private String key;
  /**
   * 配置项值
   */
  private String value;
  /**
   * 备注
   */
  private String comment;

  @Override
  public String toString() {
    return "OpenItemDTO{" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        ", comment='" + comment + '\'' +
        ", dataChangeCreatedBy='" + dataChangeCreatedBy + '\'' +
        ", dataChangeLastModifiedBy='" + dataChangeLastModifiedBy + '\'' +
        ", dataChangeCreatedTime=" + dataChangeCreatedTime +
        ", dataChangeLastModifiedTime=" + dataChangeLastModifiedTime +
        '}';
  }
}
