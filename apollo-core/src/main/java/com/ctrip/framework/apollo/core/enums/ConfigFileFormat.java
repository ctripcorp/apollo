package com.ctrip.framework.apollo.core.enums;

import com.ctrip.framework.apollo.core.utils.StringUtils;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public enum ConfigFileFormat {
  Properties("properties"), XML("xml"), JSON("json"), YML("yml"), YAML("yaml"), TXT("txt");

  private String value;

  ConfigFileFormat(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ConfigFileFormat fromString(String value) {
    if (StringUtils.isEmpty(value)) {
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

  public static boolean isValidFormat(String value) {
    try {
      fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isPropertiesCompatible(ConfigFileFormat format) {
    return format == YAML || format == YML;
  }
}
