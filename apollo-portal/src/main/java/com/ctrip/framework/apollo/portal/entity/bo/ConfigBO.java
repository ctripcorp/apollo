package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置 业务对象.
 *
 * @author wxq
 */
@AllArgsConstructor
@Getter
public class ConfigBO {

  /**
   * 环境
   */
  private final Env env;
  /**
   * 所有者名称
   */
  private final String ownerName;
  /**
   * 应用id
   */
  private final String appId;
  /**
   * 集群名称
   */
  private final String clusterName;
  /**
   * 名称空间名称
   */
  private final String namespace;
  /**
   * 配置文件内容
   */
  private final String configFileContent;
  /**
   * 配置文件格式
   */
  private final ConfigFileFormat format;


  public ConfigBO(Env env, String ownerName, String appId, String clusterName,
      NamespaceBO namespaceBO) {
    this(env, ownerName, appId, clusterName, namespaceBO.getBaseInfo().getNamespaceName(),
        NamespaceBOUtils.convert2configFileContent(namespaceBO),
        ConfigFileFormat.fromString(namespaceBO.getFormat())
    );
  }

  @Override
  public String toString() {
    return "ConfigBO{" +
        "env=" + env +
        ", ownerName='" + ownerName + '\'' +
        ", appId='" + appId + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespace='" + namespace + '\'' +
        ", configFileContent='" + configFileContent + '\'' +
        ", format=" + format +
        '}';
  }
}
