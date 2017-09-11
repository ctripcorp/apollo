package com.ctrip.framework.foundation.internals;

import com.google.common.base.Strings;

public class Utils {
  public static boolean isBlank(String str) {
    return Strings.nullToEmpty(str).trim().isEmpty();
  }

  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  public static boolean isOSWindows() {
    String osName = System.getProperty("os.name");
    return Utils.isNotBlank(osName) && osName.startsWith("Windows");
  }
}
