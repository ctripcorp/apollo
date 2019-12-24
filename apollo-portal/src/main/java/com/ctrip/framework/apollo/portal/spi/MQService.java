package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;

public interface MQService {

  void sendPublishMsg(Env env, ReleaseHistoryBO releaseHistory);

}
