package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.model.Verifiable;
import com.ctrip.framework.apollo.portal.environment.Env;
import lombok.Getter;
import lombok.Setter;

/**
 * 名称空间标识
 */
@Setter
public class NamespaceIdentifier implements Verifiable {

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


  public Env getEnv() {
    return Env.valueOf(env);
  }

  @Override
  public boolean isInvalid() {
    return StringUtils.isContainEmpty(env, clusterName, namespaceName);
  }

  @Override
  public String toString() {
    return String
        .format("NamespaceIdentifer{appId='%s', env='%s', clusterName='%s', namespaceName='%s'}",
            appId, env, clusterName, namespaceName);
  }
}
