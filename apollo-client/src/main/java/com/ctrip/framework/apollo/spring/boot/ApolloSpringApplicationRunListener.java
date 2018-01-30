package com.ctrip.framework.apollo.spring.boot;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.google.common.base.Splitter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Inject the Apollo config in Spring Boot bootstrap phase
 *
 * <p>Configuration example:</p>
 * <pre class="code">
 *   # will inject 'application' namespace in bootstrap phase
 *   apollo.bootstrap.enabled = true
 * </pre>
 *
 * or
 *
 * <pre class="code">
 *   apollo.bootstrap.enabled = true
 *   # will inject 'application' and 'TEST1.apollo' namespaces in bootstrap phase
 *   apollo.bootstrap.namespaces = application,FX.apollo
 * </pre>
 */
public class ApolloSpringApplicationRunListener implements SpringApplicationRunListener,
    PriorityOrdered {
  private static final Logger logger = LoggerFactory.getLogger(ApolloSpringApplicationRunListener.class);
  private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

  public ApolloSpringApplicationRunListener(SpringApplication application, String[] args) {
    //ignore
  }

  public void starting() {
  }

  public void started() {
  }

  public void environmentPrepared(ConfigurableEnvironment environment) {
  }

  public void contextPrepared(ConfigurableApplicationContext context) {
    ConfigurableEnvironment environment = context.getEnvironment();
    String enabled = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, "false");
    if (!Boolean.valueOf(enabled)) {
      logger.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}", context, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
      return;
    }
    logger.debug("Apollo bootstrap config is enabled for context {}, see property: ${{}}", context, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);

    if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
      //already initialized
      return;
    }

    String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
    logger.debug("Apollo bootstrap namespaces: {}", namespaces);
    List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

    CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
    for (String namespace : namespaceList) {
      Config config = ConfigService.getConfig(namespace);

      composite.addPropertySource(new ConfigPropertySource(namespace, config));
    }

    environment.getPropertySources().addFirst(composite);
  }

  public void contextLoaded(ConfigurableApplicationContext context) {
  }

  public void finished(ConfigurableApplicationContext configurableApplicationContext,
      Throwable throwable) {
  }

  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
