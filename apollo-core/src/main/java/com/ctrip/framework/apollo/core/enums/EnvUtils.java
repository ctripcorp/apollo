package com.ctrip.framework.apollo.core.enums;


import org.apache.commons.lang.StringUtils;

/**
 * 环境枚举工具类.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public final class EnvUtils {

  private EnvUtils() {
  }

  public static Env transformEnv(String envName) {
    if (StringUtils.isBlank(envName)) {
      return Env.UNKNOWN;
    }
    switch (envName.trim().toUpperCase()) {
      case "LPT":
        return Env.LPT;
      case "FAT":
      case "FWS":
        return Env.FAT;
      case "UAT":
        return Env.UAT;
      case "PRO":
        //just in case
      case "PROD":
        return Env.PRO;
      case "DEV":
        return Env.DEV;
      case "LOCAL":
        return Env.LOCAL;
      case "TOOLS":
        return Env.TOOLS;
      default:
        return Env.UNKNOWN;
    }
  }
}
