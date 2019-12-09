package com.ctrip.framework.apollo.core.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnvUtils {

  private static final Logger logger = LoggerFactory.getLogger(EnvUtils.class);

  /**
   * add some change to environment name
   * trim and to upper
   * @param environmentName
   * @return
   */
  static String getWellFormName(String environmentName) {
    return environmentName.trim().toUpperCase();
  }

  /**
   * wrapper of {@code Env.valueOf}
   * Return {@code Env.UNKNOWN} instead of throwing IllegalArgumentException
   * when environment not existed.
   * @param name
   * @return
   */
  public static Env transformEnv(String name) {
    if(Env.exist(name)) {
      return Env.valueOf(name);
    } else {
      logger.info("environment [{}] not exist, so give you [{}]", name, Env.UNKNOWN);
      return Env.UNKNOWN;
    }
  }
}
