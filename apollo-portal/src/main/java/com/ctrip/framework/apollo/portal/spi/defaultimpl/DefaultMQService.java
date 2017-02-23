package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.spi.MQService;

public class DefaultMQService implements MQService{

  @Override
  public void sendPublishMsg(Env env, ReleaseHistoryBO releaseHistory) {
    //do nothing
  }

}
