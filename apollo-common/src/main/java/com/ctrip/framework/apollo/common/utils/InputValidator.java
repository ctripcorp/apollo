package com.ctrip.framework.apollo.common.utils;

import com.ctrip.framework.apollo.core.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class InputValidator {
  public static final String INVALID_CLUSTER_NAMESPACE_MESSAGE = "Only digits, alphabets and symbol - _ . are allowed";
  public static final String INVALID_NAMESPACE_NAMESPACE_MESSAGE = "not allowed to end with .json, .yml, .yaml, .xml, .properties";
  public static final String CLUSTER_NAMESPACE_VALIDATOR = "[0-9a-zA-Z_.-]+";
  private static final String APP_NAMESPACE_VALIDATOR = "[a-zA-Z0-9._-]+(?<!\\.(json|yml|yaml|xml|properties))$";
  private static final Pattern CLUSTER_NAMESPACE_PATTERN = Pattern.compile(CLUSTER_NAMESPACE_VALIDATOR);
  private static final Pattern APP_NAMESPACE_PATTERN = Pattern.compile(APP_NAMESPACE_VALIDATOR);

  public static boolean isValidClusterNamespace(String name) {
    if (StringUtils.isEmpty(name)){
      return false;
    }
    return CLUSTER_NAMESPACE_PATTERN.matcher(name).matches();
  }

  public static boolean isValidAppNamespace(String name){
    if (StringUtils.isEmpty(name)){
      return false;
    }
    return APP_NAMESPACE_PATTERN.matcher(name).matches();
  }
}
