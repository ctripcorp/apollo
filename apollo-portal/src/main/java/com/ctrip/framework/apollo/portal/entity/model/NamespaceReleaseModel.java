package com.ctrip.framework.apollo.portal.entity.model;


import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.environment.Env;
import lombok.Getter;
import lombok.Setter;

/**
 * 名称空间发布信息 Model
 */
@Setter
public class NamespaceReleaseModel implements Verifiable {

  /**
   * 应用id
   */
  @Getter
  private String appId;
  /**
   * 环境
   */
  private String env;
  /**
   * 集群名称
   */
  @Getter
  private String clusterName;
  /**
   * 名称空间名称
   */
  @Getter
  private String namespaceName;
  /**
   * 发布标题
   */
  @Getter
  private String releaseTitle;
  /**
   * 发布说明
   */
  @Getter
  private String releaseComment;
  /**
   * 发布者
   */
  @Getter
  private String releasedBy;
  /**
   * 是否紧急发布
   */
  @Getter
  private Boolean isEmergencyPublish;

  @Override
  public boolean isInvalid() {
    return StringUtils.isContainEmpty(appId, env, clusterName, namespaceName, releaseTitle);
  }

  public Env getEnv() {
    return Env.valueOf(env);
  }
}