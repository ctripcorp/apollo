package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.common.customize.LoggingCustomizer;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ctrip")
public class BizLoggingCustomizer extends LoggingCustomizer {

  @Autowired
  private PortalConfig portalConfig;

  @Override
  protected String cloggingUrl() {
    return portalConfig.cloggingUrl();
  }

  @Override
  protected String cloggingPort() {
    return portalConfig.cloggingPort();
  }
}
