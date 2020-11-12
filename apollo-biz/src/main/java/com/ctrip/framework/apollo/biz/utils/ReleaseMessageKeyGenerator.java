package com.ctrip.framework.apollo.biz.utils;


import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Joiner;

/**
 * 发布消息KEY生成器
 */
public class ReleaseMessageKeyGenerator {

  /**
   * 字符拼接器
   */
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

  /**
   * 生成KEY
   *
   * @param appId     应用id
   * @param cluster   集群id
   * @param namespace 名称空间
   * @return 生成好的KEY
   */
  public static String generate(String appId, String cluster, String namespace) {
    return STRING_JOINER.join(appId, cluster, namespace);
  }
}
