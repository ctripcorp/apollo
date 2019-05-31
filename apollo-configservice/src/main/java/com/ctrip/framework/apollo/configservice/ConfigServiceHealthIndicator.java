package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.biz.service.AppService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class ConfigServiceHealthIndicator implements HealthIndicator {

  private final AppService appService;

  public ConfigServiceHealthIndicator(final AppService appService) {
    this.appService = appService;
  }

  @Override
  public Health health() {
    int errorCode = check();
    if (errorCode != 0) {
      return Health.down().withDetail("Error Code", errorCode).build();
    }
    return Health.up().build();
  }

  private int check() {
    PageRequest pageable = PageRequest.of(0, 1);
    appService.findAll(pageable);
    return 0;
  }

}
