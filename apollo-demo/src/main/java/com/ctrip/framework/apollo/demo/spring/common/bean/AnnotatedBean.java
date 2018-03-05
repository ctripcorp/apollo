package com.ctrip.framework.apollo.demo.spring.common.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component("annotatedBean")
public class AnnotatedBean {
  private static final Logger logger = LoggerFactory.getLogger(AnnotatedBean.class);

  private int timeout;
  private int batch;

  @Value("${batch:100}")
  public void setBatch(int batch) {
    logger.info("updating batch, old value: {}, new value: {}", this.batch, batch);
    this.batch = batch;
  }

  @Value("${timeout:200}")
  public void setTimeout(int timeout) {
    logger.info("updating timeout, old value: {}, new value: {}", this.timeout, timeout);
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    return String.format("[AnnotatedBean] timeout: %d, batch: %d", timeout, batch);
  }
}
