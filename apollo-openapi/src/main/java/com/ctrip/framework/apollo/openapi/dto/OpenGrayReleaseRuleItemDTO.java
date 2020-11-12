package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;
import lombok.Data;

/**
 * 开放的灰度发布规则明细 Dto
 */
@Data
public class OpenGrayReleaseRuleItemDTO {

  /**
   * 客户端应用id
   */
  private String clientAppId;
  /**
   * 客户端IP列表
   */
  private Set<String> clientIpList;

  public String getClientAppId() {
    return clientAppId;
  }

  public void setClientAppId(String clientAppId) {
    this.clientAppId = clientAppId;
  }

  public Set<String> getClientIpList() {
    return clientIpList;
  }

  public void setClientIpList(Set<String> clientIpList) {
    this.clientIpList = clientIpList;
  }
}
