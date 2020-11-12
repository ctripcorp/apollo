package com.ctrip.framework.apollo.biz.utils;


import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.common.utils.UniqueKeyGenerator;

/**
 * 发布id生成器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ReleaseKeyGenerator extends UniqueKeyGenerator {


  /**
   * Generate the release key in the format: timestamp+appId+cluster+namespace+hash(ipAsInt+counter)
   *
   * @param namespace the namespace of the release
   * @return the unique release key
   */
  public static String generateReleaseKey(Namespace namespace) {
    return generate(namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());
  }
}
