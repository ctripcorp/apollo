package com.ctrip.framework.apollo.biz.customize;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.common.customize.LoggingCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ctrip")
public class BizLoggingCustomizer extends LoggingCustomizer{

  private final BizConfig bizConfig;

  public BizLoggingCustomizer(final BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  @Override
  protected String cloggingUrl() {
    return bizConfig.cloggingUrl();
  }

  @Override
  protected String cloggingPort() {
    return bizConfig.cloggingPort();
  }
}
