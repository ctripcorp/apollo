package com.ctrip.framework.apollo.common.config;

import com.google.common.base.Splitter;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;


public abstract class RefreshableConfig {

  private static final Logger logger = LoggerFactory.getLogger(RefreshableConfig.class);

  private static final String LIST_SEPARATOR = ",";
  //TimeUnit: second
  private static final int CONFIG_REFRESH_INTERVAL = 60;

  protected Splitter splitter = Splitter.on(LIST_SEPARATOR).omitEmptyStrings().trimResults();

  @Autowired
  private ConfigurableEnvironment environment;

  private List<RefreshablePropertySource> propertySources;

  /**
   * register refreshable property source.
   * Notice: The front property source has higher priority.
   */
  protected abstract List<RefreshablePropertySource> getRefreshablePropertySources();

  @PostConstruct
  public void setup() {

    propertySources = getRefreshablePropertySources();
    if (CollectionUtils.isEmpty(propertySources)) {
      throw new IllegalStateException("Property sources can not be empty.");
    }

    //add property source to environment
    for (RefreshablePropertySource propertySource : propertySources) {
      propertySource.refresh();
      environment.getPropertySources().addLast(propertySource);
    }

    //task to update configs
    ScheduledExecutorService
        executorService =
        Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("ConfigRefresher", false));

    executorService
        .scheduleWithFixedDelay(() -> {
          try {
            propertySources.forEach(RefreshablePropertySource::refresh);
          } catch (Throwable t) {
            logger.error("Refresh configs failed.", t);
            Tracer.logError("Refresh configs failed.", t);
          }
        }, CONFIG_REFRESH_INTERVAL, CONFIG_REFRESH_INTERVAL, TimeUnit.SECONDS);
  }

  public int getIntProperty(String key, int defaultValue) {
    try {
      String value = getValue(key);
      return value == null ? defaultValue : Integer.parseInt(value);
    } catch (Exception e) {
      Tracer.logError("Get int property failed.", e);
      return defaultValue;
    }
  }

  public boolean getBooleanProperty(String key, boolean defaultValue) {
    try {
      String value = getValue(key);
      return value == null ? defaultValue : "true".equals(value);
    } catch (Exception e) {
      Tracer.logError("Get boolean property failed.", e);
      return defaultValue;
    }
  }

  public String[] getArrayProperty(String key, String[] defaultValue) {
    try {
      String value = getValue(key);
      return Strings.isNullOrEmpty(value) ? defaultValue : value.split(LIST_SEPARATOR);
    } catch (Exception e) {
      Tracer.logError("Get array property failed.", e);
      return defaultValue;
    }
  }

  public String getValue(String key, String defaultValue) {
    try {
      return environment.getProperty(key, defaultValue);
    } catch (Exception e) {
      Tracer.logError("Get value failed.", e);
      return defaultValue;
    }
  }

  public String getValue(String key) {
    return environment.getProperty(key);
  }

}
