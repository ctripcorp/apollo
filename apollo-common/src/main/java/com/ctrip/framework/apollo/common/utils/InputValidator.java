package com.ctrip.framework.apollo.common.utils;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * 输入验证
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class InputValidator {

  /**
   * 非法集群名称（只有数字、字母和符号-。是允许的）
   */
  public static final String INVALID_CLUSTER_NAMESPACE_MESSAGE = "Only digits, alphabets and symbol - _ . are allowed";
  /**
   * 非法的名称空间消息（不允许以.json、.yml、.yaml、.xml、.properties结尾）
   */
  public static final String INVALID_NAMESPACE_NAMESPACE_MESSAGE = "not allowed to end with .json, .yml, .yaml, .xml, .properties";
  /**
   * 集群名称空间验证
   */
  public static final String CLUSTER_NAMESPACE_VALIDATOR = "[0-9a-zA-Z_.-]+";
  /**
   * 应用名称空间验证
   */
  private static final String APP_NAMESPACE_VALIDATOR = "[a-zA-Z0-9._-]+(?<!\\.(json|yml|yaml|xml|properties))$";
  /**
   * 集群名称空间匹配对象
   */
  private static final Pattern CLUSTER_NAMESPACE_PATTERN = Pattern
      .compile(CLUSTER_NAMESPACE_VALIDATOR);
  /**
   * 应用名称空间匹配对象
   */
  private static final Pattern APP_NAMESPACE_PATTERN = Pattern.compile(APP_NAMESPACE_VALIDATOR);


  /**
   * 是否为集群名称空间
   *
   * @param name 名称空间
   * @return 配置成功，true,否则，false
   */
  public static boolean isValidClusterNamespace(String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    return CLUSTER_NAMESPACE_PATTERN.matcher(name).matches();
  }

  /**
   * 是否为应用名称空间
   *
   * @param name 名称空间
   * @return 配置成功，true,否则，false
   */
  public static boolean isValidAppNamespace(String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    return APP_NAMESPACE_PATTERN.matcher(name).matches();
  }
}
