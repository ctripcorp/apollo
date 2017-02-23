package com.ctrip.framework.apollo.demo.spring.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class NormalBean {
  private static final Logger logger = LoggerFactory.getLogger(NormalBean.class);

  @Value("${timeout:200}")
  private int timeout;
  private int batch;

  @PostConstruct
  void initialize() {
    logger.info("timeout is {}", timeout);
    logger.info("batch is {}", batch);
  }

  public void setBatch(int batch) {
    this.batch = batch;
  }
}
