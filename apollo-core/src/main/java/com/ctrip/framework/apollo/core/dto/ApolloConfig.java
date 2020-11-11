package com.ctrip.framework.apollo.core.dto;

import java.util.Map;
import lombok.Data;

/**
 * apollo配置信息
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class ApolloConfig {

  /**
   * 应用id
   */
  private String appId;
  /**
   * 集群
   */
  private String cluster;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 配置集
   */
  private Map<String, String> configurations;
  /**
   * 发布的key
   */
  private String releaseKey;

  public ApolloConfig(String appId,
      String cluster,
      String namespaceName,
      String releaseKey) {
    this.appId = appId;
    this.cluster = cluster;
    this.namespaceName = namespaceName;
    this.releaseKey = releaseKey;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ApolloConfig{");
    sb.append("appId='").append(appId).append('\'');
    sb.append(", cluster='").append(cluster).append('\'');
    sb.append(", namespaceName='").append(namespaceName).append('\'');
    sb.append(", configurations=").append(configurations);
    sb.append(", releaseKey='").append(releaseKey).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
