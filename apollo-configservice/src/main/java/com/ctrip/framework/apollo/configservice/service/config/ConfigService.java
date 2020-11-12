package com.ctrip.framework.apollo.configservice.service.config;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;

/**
 * 配置 Service 接口.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigService extends ReleaseMessageListener {

  /**
   * 读取指定 Namespace 的最新的 Release 对象
   *
   * @param clientAppId       客户端应用id
   * @param clientIp          客户端ip
   * @param configAppId       配置应用id
   * @param configClusterName 配置集群名称
   * @param configNamespace   配置名称空间
   * @param dataCenter        客户端数据中心
   * @param clientMessages    客户端接收的消息
   * @return 发布信息
   */
  Release loadConfig(String clientAppId, String clientIp, String configAppId, String
      configClusterName, String configNamespace, String dataCenter,
      ApolloNotificationMessages clientMessages);
}
