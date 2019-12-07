package com.ctrip.framework.apollo.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
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


  /**
   * make a filter on properties.
   * and convert properties to a map
   * @param properties
   * @param suffix suffix in a key
   * @return a map which key is ends with suffix
   */
  public static Map<String, String> filterWithKeyEndswith(Properties properties, String suffix) {
    // use O(n log(n)) algorithm
    Map<String, String> map = new HashMap<>();
    for(String propertyName : properties.stringPropertyNames()) {
      if(propertyName.endsWith(suffix)) {
        map.put(propertyName, properties.getProperty(propertyName));
      }
    }
    return map;
  }

  /**
   * make a filter on map's key,
   * keep the k-v which key ends with suffix given
   * @param keyValues
   * @param suffix suffix in a key
   * @return a map which key is ends with suffix
   */
  public static Map<String, String> filterWithKeyEndswith(Map<String, String> keyValues, String suffix) {
    // use O(n) algorithm
    Map<String, String> map = new HashMap<>();
    for(Map.Entry<String, String> entry : keyValues.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if(key.endsWith(suffix)) {
        map.put(key, value);
      }
    }
    return map;
  }

  /**
   * remove key's suffix in a map
   * suppose that all keys's length not smaller than suffixLength,
   * if not satisfied, a terrible runtime exception will occur
   * @param keyValues
   * @param suffixLength suffix string's length
   * @return
   */
  public static Map<String, String> removeKeySuffix(Map<String, String> keyValues, int suffixLength) {
    // use O(n) algorithm
    Map<String, String> map = new HashMap<>();
    for(Map.Entry<String, String> entry : keyValues.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      String newKey = key.substring(0, key.length() - suffixLength);
      map.put(newKey, value);
    }
    return map;
  }
}
