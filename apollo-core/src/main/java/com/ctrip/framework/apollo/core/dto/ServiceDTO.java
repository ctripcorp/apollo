package com.ctrip.framework.apollo.core.dto;

import lombok.Data;

/**
 * 服务信息 Dto
 */
@Data
public class ServiceDTO {

  /**
   * 应用名称
   */
  private String appName;
  /**
   * 实例id
   */
  private String instanceId;
  /**
   * 主页地址。如homePageUrl=http://localhost:8080
   * <p>一般使用占位符形式配置xxx.homePageUrl = http://${mynamespace.hostname}:7001
   */
  private String homepageUrl;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServiceDTO{");
    sb.append("appName='").append(appName).append('\'');
    sb.append(", instanceId='").append(instanceId).append('\'');
    sb.append(", homepageUrl='").append(homepageUrl).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
