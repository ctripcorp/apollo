package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;

/**
 * MQ Service接口
 */
public interface MQService {

  /**
   * 发送发布消息
   *
   * @param env            环境
   * @param releaseHistory 发布历史信息
   */
  void sendPublishMsg(Env env, ReleaseHistoryBO releaseHistory);

}
