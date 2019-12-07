package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;

public interface MQService {

  void sendPublishMsg(String env, ReleaseHistoryBO releaseHistory);

}
