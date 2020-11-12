package com.ctrip.framework.apollo.configservice.service.config;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.core.dto.ApolloNotificationMessages;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 配置 Service 默认实现类，直接查询数据库，而不使用缓存
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigService extends AbstractConfigService {

  @Autowired
  private ReleaseService releaseService;

  @Override
  protected Release findActiveOne(long id, ApolloNotificationMessages clientMessages) {
    // 指定id并且有效的发布对象
    return releaseService.findActiveOne(id);
  }

  @Override
  protected Release findLatestActiveRelease(String configAppId, String configClusterName,
      String configNamespace, ApolloNotificationMessages clientMessages) {
    // 获得最新并且有效的 Release 对象
    return releaseService.findLatestActiveRelease(configAppId, configClusterName,
        configNamespace);
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    // since there is no cache, so do nothing
  }
}
