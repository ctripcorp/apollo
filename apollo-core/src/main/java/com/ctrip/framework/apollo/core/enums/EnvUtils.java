package com.ctrip.framework.apollo.core.enums;

import com.ctrip.framework.apollo.core.utils.StringUtils;

public final class EnvUtils {
  
  public static Env transformEnv(String envName) {
    if (StringUtils.isBlank(envName)) {
      return null;
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
      case "PROD": //just in case
        return Env.PRO;
      case "DEV":
        return Env.DEV;
      case "LOCAL":
        return Env.LOCAL;
      default:
        return null;
    }
  }
}
