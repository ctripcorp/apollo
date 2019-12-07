package com.ctrip.framework.apollo.core.utils;

public final class EnvUtils {

  /**
   * add some change to environment name
   * trim and to upper
   * @param environmentName
   * @return
   */
  public static String getWellFormName(String environmentName) {
    return environmentName.trim().toUpperCase();
  }

}
