package com.ctrip.framework.apollo.common.utils;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class InputValidator {
  public static final String INVALID_CLUSTER_NAMESPACE_MESSAGE = "只允许输入数字，字母和符号 - _ .";
  public static final String INVALID_NAMESPACE_NAMESPACE_MESSAGE = "不允许以.json, .yml, .yaml, .xml, .properties结尾";
  public static final String CLUSTER_NAMESPACE_VALIDATOR = "[0-9a-zA-Z_.-]+";
}
