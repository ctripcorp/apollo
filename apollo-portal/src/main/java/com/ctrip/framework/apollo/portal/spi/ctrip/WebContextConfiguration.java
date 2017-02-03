package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.spi.ctrip.filters.UserAccessFilter;
import com.google.common.base.Strings;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configuration
@Profile("ctrip")
public class WebContextConfiguration {

  @Autowired
  private PortalConfig portalConfig;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @Bean
  public ServletContextInitializer servletContextInitializer() {

    return new ServletContextInitializer() {

      @Override
      public void onStartup(ServletContext servletContext) throws ServletException {
        String loggingServerIP = portalConfig.cloggingUrl();
        String loggingServerPort = portalConfig.cloggingUrl();
        String credisServiceUrl = portalConfig.credisServiceUrl();

        servletContext.setInitParameter("loggingServerIP",
            Strings.isNullOrEmpty(loggingServerIP) ? "" : loggingServerIP);
        servletContext.setInitParameter("loggingServerPort",
            Strings.isNullOrEmpty(loggingServerPort) ? "" : loggingServerPort);
        servletContext.setInitParameter("credisServiceUrl",
            Strings.isNullOrEmpty(credisServiceUrl) ? "" : credisServiceUrl);
      }
    };
  }

  @Bean
  public FilterRegistrationBean userAccessFilter() {
    FilterRegistrationBean filter = new FilterRegistrationBean();
    filter.setFilter(new UserAccessFilter(userInfoHolder));
    filter.addUrlPatterns("/*");
    return filter;
  }

}
