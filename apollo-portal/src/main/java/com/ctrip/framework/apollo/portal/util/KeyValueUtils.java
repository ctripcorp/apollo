package com.ctrip.framework.apollo.portal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Map和Properties中key操作的一些工具.
 *
 * @author wxq
 */
public class KeyValueUtils {

  /**
   * 对属性进行筛选。并将属性转换为Map中key后缀匹配不区分大小写
   *
   * @param properties 属性文件
   * @param suffix     key的后缀
   * @return 以后缀结尾的Map
   */
  public static Map<String, String> filterWithKeyIgnoreCaseEndsWith(Properties properties,
      String suffix) {
    // use O(n log(n)) algorithm
    Map<String, String> keyValues = new HashMap<>();
    //获取属性中的所有k-v
    for (String propertyName : properties.stringPropertyNames()) {
      keyValues.put(propertyName, properties.getProperty(propertyName));
    }
    return filterWithKeyIgnoreCaseEndsWith(keyValues, suffix);
  }

  /**
   * 过滤map中的key，保持k-v键以指定后缀结尾，给定后缀匹配不区分大小写
   *
   * @param keyValues k-v集合 * @param suffix    key的后缀
   * @return 返回以指定后缀结尾的Map
   */
  public static Map<String, String> filterWithKeyIgnoreCaseEndsWith(Map<String, String> keyValues,
      String suffix) {
    // use O(n) algorithm
    Map<String, String> map = new HashMap<>();
    // 找到指定key后缀结尾的Map
    keyValues.forEach((key, value) -> {
      if (key.toUpperCase().endsWith(suffix.toUpperCase())) {
        map.put(key, value);
      }
    });
    return map;
  }

  /**
   * 从Map中移除key的后缀,假设所有键的长度不小于后缀长度，如果不满足，将发生一个可怕的运行时异常
   *
   * @param keyValues    k-v集合
   * @param suffixLength 后缀的长度
   * @return 返回移除指定key的后缀的k-v集合
   */
  public static Map<String, String> removeKeySuffix(Map<String, String> keyValues,
      int suffixLength) {
    // use O(n) algorithm
    Map<String, String> map = new HashMap<>();
    // 截取key中指定的长度作为新的key
    keyValues.forEach((key, value) -> {
      String newKey = key.substring(0, key.length() - suffixLength);
      map.put(newKey, value);
    });
    return map;
  }

}
