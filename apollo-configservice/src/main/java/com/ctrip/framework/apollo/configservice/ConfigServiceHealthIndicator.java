package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.biz.service.AppService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * 配置Service健康指示器
 */
@Component
public class ConfigServiceHealthIndicator implements HealthIndicator {

  private final AppService appService;

  public ConfigServiceHealthIndicator(final AppService appService) {
    this.appService = appService;
  }

  @Override
  public Health health() {
    // 设置环境状态为UP
    check();
    return Health.up().build();
  }

  /**
   * 获取全部应用信息
   */
  private void check() {
    PageRequest pageable = PageRequest.of(0, 1);
    appService.findAll(pageable);
  }

}
