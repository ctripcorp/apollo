package com.ctrip.framework.apollo.assembly;

import com.ctrip.framework.apollo.adminservice.AdminServiceApplication;
import com.ctrip.framework.apollo.configservice.ConfigServiceApplication;
import com.ctrip.framework.apollo.portal.PortalApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
public class ApolloApplication {

  private static final Logger logger = LoggerFactory.getLogger(ApolloApplication.class);

  public static void main(String[] args) throws Exception {
    /**
     * Common
     */
    ConfigurableApplicationContext commonContext =
        new SpringApplicationBuilder(ApolloApplication.class).web(WebApplicationType.NONE).run(args);
    logger.info(commonContext.getId() + " isActive: " + commonContext.isActive());

    /**
     * ConfigService
     */
    if (commonContext.getEnvironment().containsProperty("configservice")) {
      ConfigurableApplicationContext configContext =
          new SpringApplicationBuilder(ConfigServiceApplication.class).parent(commonContext)
              .sources(RefreshScope.class).run(args);
      logger.info(configContext.getId() + " isActive: " + configContext.isActive());
    }

    /**
     * AdminService
     */
    if (commonContext.getEnvironment().containsProperty("adminservice")) {
      ConfigurableApplicationContext adminContext =
          new SpringApplicationBuilder(AdminServiceApplication.class).parent(commonContext)
              .sources(RefreshScope.class).run(args);
      logger.info(adminContext.getId() + " isActive: " + adminContext.isActive());
    }

    /**
     * Portal
     */
    if (commonContext.getEnvironment().containsProperty("portal")) {
      ConfigurableApplicationContext portalContext =
          new SpringApplicationBuilder(PortalApplication.class).parent(commonContext)
              .sources(RefreshScope.class).run(args);
      logger.info(portalContext.getId() + " isActive: " + portalContext.isActive());
    }
  }

}
