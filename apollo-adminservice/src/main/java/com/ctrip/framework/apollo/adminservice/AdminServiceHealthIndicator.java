package com.ctrip.framework.apollo.adminservice;

import com.ctrip.framework.apollo.biz.service.AppService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminServiceHealthIndicator implements HealthIndicator {

  private final AppService appService;

  public AdminServiceHealthIndicator(final AppService appService) {
    this.appService = appService;
  }

  @Override
  public Health health() {
    check();
    return Health.up().build();
  }

  private void check() {
    PageRequest pageable = PageRequest.of(0, 1);
    appService.findAll(pageable);
  }

}
