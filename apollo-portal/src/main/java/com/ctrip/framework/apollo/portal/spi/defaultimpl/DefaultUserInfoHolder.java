package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import lombok.NoArgsConstructor;

/**
 * 默认用户信息持有器
 */
@NoArgsConstructor
public class DefaultUserInfoHolder implements UserInfoHolder {

  @Override
  public UserInfo getUser() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("apollo");
    return userInfo;
  }
}
