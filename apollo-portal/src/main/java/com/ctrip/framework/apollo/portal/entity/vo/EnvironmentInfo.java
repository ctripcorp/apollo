package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import lombok.Data;

/**
 * 环境信息
 */
@Data
public class EnvironmentInfo {

  /**
   * 环境
   */
  private String env;
  /**
   * 是否活跃
   */
  private boolean active;
  /**
   * 元服务器地址
   */
  private String metaServerAddress;
  /**
   * 配置服务信息
   */
  private ServiceDTO[] configServices;
  /**
   * 系统服务信息
   */
  private ServiceDTO[] adminServices;
  /**
   * 错误消息
   */
  private String errorMessage;
}
