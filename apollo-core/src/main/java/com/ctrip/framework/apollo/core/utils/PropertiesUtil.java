package com.ctrip.framework.apollo.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertiesUtil {
  /**
   * Transform the properties to string format
   * @param properties the properties object
   * @return the string containing the properties
   * @throws IOException
   */
  public static String toString(Properties properties) throws IOException {
    StringWriter writer = new StringWriter();
    properties.store(writer, null);
    StringBuffer stringBuffer = writer.getBuffer();
    filterPropertiesComment(stringBuffer);
    return stringBuffer.toString();
  }

  /**
   * Transform the properties to string format
   * @param properties the LinkedHashMap object
   * @return the string containing the properties
   * @throws IOException
   */
  public static String toString(LinkedHashMap properties) throws IOException {
    StringWriter writer = new StringWriter();
    Properties toWrite=new Properties();
    toWrite.putAll(properties);
    toWrite.store(writer, null);
    StringBuffer stringBuffer = writer.getBuffer();
    filterPropertiesComment(stringBuffer);
    return stringBuffer.toString();
  }

  /**
   * filter out the first comment line
   * @param stringBuffer the string buffer
   * @return true if filtered successfully, false otherwise
   */
  static boolean filterPropertiesComment(StringBuffer stringBuffer) {
    //check whether has comment in the first line
    if (stringBuffer.charAt(0) != '#') {
      return false;
    }
    int commentLineIndex = stringBuffer.indexOf("\n");
    if (commentLineIndex == -1) {
      return false;
    }
    stringBuffer.delete(0, commentLineIndex + 1);
    return true;
  }
}
