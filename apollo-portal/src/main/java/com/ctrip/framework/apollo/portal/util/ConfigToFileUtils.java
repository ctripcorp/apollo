package com.ctrip.framework.apollo.portal.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置转文件工具类 jian.tan
 */

public class ConfigToFileUtils {

  private ConfigToFileUtils() {
  }

  /**
   * 配置项转文件（实际也就是输出打印了一下内容）
   *
   * @param os    输出流
   * @param items 配置项列表
   * @deprecated
   */
  @Deprecated
  public static void itemsToFile(OutputStream os, List<String> items) {
    try (PrintWriter printWriter = new PrintWriter(os)) {
      items.forEach(printWriter::println);
    }
  }

  /**
   * 将输入流转换为字符串
   *
   * @param inputStream 输入流
   * @return 转换的字符串
   */
  public static String fileToString(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
  }
}
