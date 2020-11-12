package com.ctrip.framework.apollo.core.utils;

public class StringUtils {

  public static final String EMPTY = "";

  /**
   * 判断指定字符串数组存在空数据
   *
   * @param args 字符串数组
   * @return true, 存在空字符，否则，false
   */
  public static boolean isContainEmpty(String... args) {
    if (args == null) {
      return false;
    }
    for (String arg : args) {
      if (org.apache.commons.lang.StringUtils.isBlank(arg)) {
        return true;
      }
    }
    return false;
  }
}
