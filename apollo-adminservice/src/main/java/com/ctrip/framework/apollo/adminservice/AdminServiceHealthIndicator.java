package com.ctrip.framework.apollo.adminservice;

import com.ctrip.framework.apollo.biz.service.AppService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * 系统服务的自定义健康指示器
 */
@Component
public class AdminServiceHealthIndicator implements HealthIndicator {

  private final AppService appService;

  public AdminServiceHealthIndicator(final AppService appService) {
    this.appService = appService;
  }

  @Override
  public Health health() {
    // 自定义健康检查逻辑
    check();
    return Health.up().build();
  }

  /**
   * 检查应用是否存在，不存在会抛出空指针
   */
  private void check() {
    PageRequest pageable = PageRequest.of(0, 1);
    appService.findAll(pageable);
  }

}
