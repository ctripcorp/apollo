package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;

public class ConfigBO {

  private final Env env;

  private final String ownerName;

  private final String appId;

  private final String clusterName;

  private final String namespace;

  private final String configFileContent;

  private final ConfigFileFormat format;

  public ConfigBO() {
    this.env = null;
    this.ownerName = null;
    this.appId = null;
    this.clusterName = null;
    this.namespace = null;
    this.configFileContent = null;
    this.format = null;
  }

  public ConfigBO(Env env, String ownerName, String appId, String clusterName,
      String namespace, String configFileContent, ConfigFileFormat format) {
    this.env = env;
    this.ownerName = ownerName;
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespace = namespace;
    this.configFileContent = configFileContent;
    this.format = format;
  }

  public ConfigBO(Env env, String ownerName, String appId, String clusterName, NamespaceBO namespaceBO) {
    this.env = env;
    this.ownerName = ownerName;
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespace = namespaceBO.getBaseInfo().getNamespaceName();
    this.configFileContent = NamespaceBOUtils.convert2configFileContent(namespaceBO);
    this.format = ConfigFileFormat.fromString(namespaceBO.getFormat());
  }

  public boolean isFinished() {
    return null == this.env
        || null == this.ownerName
        || null == this.appId
        || null == this.clusterName
        || null == this.namespace
        || null == this.configFileContent
        || null == this.format;
  }

  public Env getEnv() {
    return env;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getConfigFileContent() {
    return configFileContent;
  }

  public ConfigFileFormat getFormat() {
    return format;
  }
}
