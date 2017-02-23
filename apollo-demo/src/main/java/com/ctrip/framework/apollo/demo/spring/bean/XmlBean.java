package com.ctrip.framework.apollo.demo.spring.bean;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlBean implements InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(XmlBean.class);
  private int timeout;
  private int batch;
  @ApolloConfig
  private Config config;
  @ApolloConfig("FX.apollo")
  private Config anotherConfig;

  public void setTimeout(int timeout) {
    this.timeout = timeout;
    logger.info("Setting timeout to {}", timeout);
  }

  public void setBatch(int batch) {
    this.batch = batch;
    logger.info("Setting batch to {}", batch);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    logger.info("Keys for config: {}", config.getPropertyNames());
    logger.info("Keys for anotherConfig: {}", anotherConfig.getPropertyNames());
  }

  @ApolloConfigChangeListener("application")
  private void someChangeHandler(ConfigChangeEvent changeEvent) {
    logger.info("[someChangeHandler]Changes for namespace {}", changeEvent.getNamespace());
    for (String key : changeEvent.changedKeys()) {
      ConfigChange change = changeEvent.getChange(key);
      logger.info("[someChangeHandler]Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
          change.getPropertyName(), change.getOldValue(), change.getNewValue(),
          change.getChangeType());
    }
  }

  @ApolloConfigChangeListener({"application", "FX.apollo"})
  private void anotherChangeHandler(ConfigChangeEvent changeEvent) {
    logger.info("[anotherChangeHandler]Changes for namespace {}", changeEvent.getNamespace());
    for (String key : changeEvent.changedKeys()) {
      ConfigChange change = changeEvent.getChange(key);
      logger.info("[anotherChangeHandler]Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
          change.getPropertyName(), change.getOldValue(), change.getNewValue(),
          change.getChangeType());
    }
  }
}
