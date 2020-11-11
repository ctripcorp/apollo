package com.ctrip.framework.apollo.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * properties工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertiesUtil {

  /**
   * 将属性转换为字符串格式
   *
   * @param properties Properties对象
   * @return 包含属性的字符串
   * @throws IOException 如果将此属性列表写入指定的输出流将引发IOException，抛出
   */
  public static String toString(Properties properties) throws IOException {
    StringWriter writer = new StringWriter();
    properties.store(writer, null);
    StringBuffer stringBuffer = writer.getBuffer();
    // 去除头部自动添加的注释
    filterPropertiesComment(stringBuffer);
    return stringBuffer.toString();
  }

  /**
   * 过滤掉第一行注释
   *
   * @param stringBuffer StringBuffer对象
   * @return 如果过滤成功，则为true；否则为false
   */
  static boolean filterPropertiesComment(StringBuffer stringBuffer) {
    // 检查第一行是否有注释
    if (stringBuffer.charAt(0) != '#') {
      return false;
    }
    // 换行的下标
    int commentLineIndex = stringBuffer.indexOf("\n");
    if (commentLineIndex == -1) {
      return false;
    }
    // 删除注释
    stringBuffer.delete(0, commentLineIndex + 1);
    return true;
  }
}
