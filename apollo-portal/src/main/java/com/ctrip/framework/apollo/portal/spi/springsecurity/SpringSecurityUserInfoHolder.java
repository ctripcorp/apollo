package com.ctrip.framework.apollo.portal.spi.springsecurity;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class SpringSecurityUserInfoHolder implements UserInfoHolder {

  @Override
  public UserInfo getUser() {
    UserInfo userInfo = new UserInfo();
    String userId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    userInfo.setUserId(userId);
    return userInfo;
  }
}
