package com.ctrip.framework.apollo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/19
 */
public class DeferredLoggerFactory {

  private DeferredLoggerFactory() {
  }

  public static Logger getLogger(Class<?> clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);
    return new DeferredLogger(logger);
  }

  public static Logger getLogger(String name) {
    Logger logger = LoggerFactory.getLogger(name);
    return new DeferredLogger(logger);
  }
}
