package com.ctrip.framework.foundation.internals;

import com.google.common.base.Strings;

public class Utils {

  /**
   * 字符串是否为空
   *
   * @param str 指定字符串
   * @return 字符串为null或者有空字符，返回true,否则，false
   */
  public static boolean isBlank(String str) {
    return Strings.nullToEmpty(str).trim().isEmpty();
  }

  /**
   * 判断是否为Windows操作系统，
   *
   * @return true, 表示为Windows系统，否则，false
   */
  public static boolean isOSWindows() {
    // 获取操作系统名称
    String osName = System.getProperty("os.name");
    if (Utils.isBlank(osName)) {
      return false;
    }
    return osName.startsWith("Windows");
  }

  public static void main(String[] args) {
    isOSWindows();
  }
}
