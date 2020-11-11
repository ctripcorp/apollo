package com.ctrip.framework.apollo.openapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开放的应用信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenAppDTO extends BaseDTO {

  /**
   * 应用名
   */
  private String name;
  /**
   * 应用Id
   */
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("OpenAppDTO{");
    sb.append("name='").append(name).append('\'');
    sb.append(", appId='").append(appId).append('\'');
    sb.append(", orgId='").append(orgId).append('\'');
    sb.append(", orgName='").append(orgName).append('\'');
    sb.append(", ownerName='").append(ownerName).append('\'');
    sb.append(", ownerEmail='").append(ownerEmail).append('\'');
    sb.append(", dataChangeCreatedBy='").append(dataChangeCreatedBy).append('\'');
    sb.append(", dataChangeLastModifiedBy='").append(dataChangeLastModifiedBy).append('\'');
    sb.append(", dataChangeCreatedTime=").append(dataChangeCreatedTime);
    sb.append(", dataChangeLastModifiedTime=").append(dataChangeLastModifiedTime);
    sb.append('}');
    return sb.toString();
  }
}
