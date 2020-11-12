package com.ctrip.framework.apollo.portal.entity.model;


import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.environment.Env;
import lombok.Getter;
import lombok.Setter;

/**
 * 名称空间文本 model
 */
@Setter
public class NamespaceTextModel implements Verifiable {

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
   * 名称空间id
   */
  @Getter
  private long namespaceId;
  /**
   * 名称空间格式（后缀）类型
   */
  private String format;
  /**
   * 配置项文本
   */
  @Getter
  private String configText;


  @Override
  public boolean isInvalid() {
    return StringUtils.isContainEmpty(appId, env, clusterName, namespaceName) || namespaceId <= 0;
  }

  public Env getEnv() {
    return Env.valueOf(env);
  }


  public ConfigFileFormat getFormat() {
    return ConfigFileFormat.fromString(this.format);
  }
}
