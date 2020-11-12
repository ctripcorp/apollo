package com.ctrip.framework.apollo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * 配置文件格式枚举.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Getter
@AllArgsConstructor
public enum ConfigFileFormat {
  /**
   * properties文件
   */
  Properties("properties"),
  /**
   * xml文件
   */
  XML("xml"),
  /**
   * json文件
   */
  JSON("json"),
  /**
   * yml文件
   */
  YML("yml"),
  /**
   * yaml文件
   */
  YAML("yaml"),
  /**
   * txt文件
   */
  TXT("txt");
  /**
   * 文件格式字符串
   */
  private String value;

  public static ConfigFileFormat fromString(String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException("value can not be empty");
    }
    switch (value.toLowerCase()) {
      case "properties":
        return Properties;
      case "xml":
        return XML;
      case "json":
        return JSON;
      case "yml":
        return YML;
      case "yaml":
        return YAML;
      case "txt":
        return TXT;
    }
    throw new IllegalArgumentException(value + " can not map enum");
  }

  /**
   * 验证是否为可用的文件格式
   *
   * @param value 文件格式字符串
   * @return 如果是可用的文件格式, 返回true.否则, 返回false
   */
  public static boolean isValidFormat(String value) {
    try {
      fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 属性文件是否兼容
   *
   * @param format 格式枚举
   * @return 如果为YAML或者YML，返回true,否则，false
   */
  public static boolean isPropertiesCompatible(ConfigFileFormat format) {
    return format == YAML || format == YML;
  }
}
