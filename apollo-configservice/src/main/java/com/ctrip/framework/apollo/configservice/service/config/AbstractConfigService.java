package com.ctrip.framework.apollo.configservice.service.config;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import com.google.common.base.Strings;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 配置 Service 抽象类，实现公用的获取配置的逻辑，并暴露抽象方法，让子类实现
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigService implements ConfigService {

  @Autowired
  private GrayReleaseRulesHolder grayReleaseRulesHolder;

  @Override
  public Release loadConfig(String clientAppId, String clientIp, String configAppId,
      String configClusterName, String configNamespace, String dataCenter,
      ApolloNotificationMessages clientMessages) {
    //  // 优先，获得指定 Cluster 的 Release 。若存在，直接返回。
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, configClusterName)) {
      Release clusterRelease = findRelease(clientAppId, clientIp, configAppId, configClusterName,
          configNamespace,
          clientMessages);

      if (!Objects.isNull(clusterRelease)) {
        return clusterRelease;
      }
    }

    // try to load via data center
    // 其次，获得所属 IDC 的 Cluster 的 Release 。若存在，直接返回
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, configClusterName)) {
      Release dataCenterRelease = findRelease(clientAppId, clientIp, configAppId, dataCenter,
          configNamespace,
          clientMessages);
      if (!Objects.isNull(dataCenterRelease)) {
        return dataCenterRelease;
      }
    }

    // 最后，获得默认 Cluster 的 Release 。
    // fallback to default release
    return findRelease(clientAppId, clientIp, configAppId, ConfigConsts.CLUSTER_NAME_DEFAULT,
        configNamespace,
        clientMessages);
  }

  /**
   * 获得发布对象
   *
   * @param clientAppId       客户端应用id
   * @param clientIp          客户端ip
   * @param configAppId       配置应用id
   * @param configClusterName 配置集群名称
   * @param configNamespace   配置名称空间
   * @param clientMessages    客户端的消息
   * @return 发布对象
   */
  private Release findRelease(String clientAppId, String clientIp, String configAppId,
      String configClusterName, String configNamespace, ApolloNotificationMessages clientMessages) {
    // 读取灰度发布id
    Long grayReleaseId = grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(clientAppId,
        clientIp, configAppId, configClusterName, configNamespace);

    //  读取灰度 Release 对象
    Release release = null;
    if (grayReleaseId != null) {
      release = findActiveOne(grayReleaseId, clientMessages);
    }

    // 非灰度，获得最新的，并且有效的 Release 对象
    if (release == null) {
      release = findLatestActiveRelease(configAppId, configClusterName, configNamespace,
          clientMessages);
    }

    return release;
  }

  /**
   * 获得指定id，并且有效的 Release 对象
   *
   * @param id             发布id
   * @param clientMessages 客户端消息列表
   * @return 发布信息
   */
  protected abstract Release findActiveOne(long id, ApolloNotificationMessages clientMessages);

  /**
   * 获得最新的，并且有效的 Release 对象
   *
   * @param configAppId         配置应用id
   * @param configClusterName   配置集群名称
   * @param configNamespaceName 配置名称空间名称
   * @param clientMessages      客户端消息列表
   * @return 发布信息
   */
  protected abstract Release findLatestActiveRelease(String configAppId, String configClusterName,
      String configNamespaceName, ApolloNotificationMessages clientMessages);
}
