package com.ctrip.framework.apollo.portal.spi.springsecurity;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;

public class SpringSecurityUserInfoHolder implements UserInfoHolder {

  private final UserService userService;

  public SpringSecurityUserInfoHolder(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserInfo getUser() {
    String userId = this.getCurrentUsername();
    UserInfo userInfoFound = this.userService.findByUserId(userId);
    if (userInfoFound != null) {
      return userInfoFound;
    }
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(userId);
    return userInfo;
  }

  private String getCurrentUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }
    if (principal instanceof Principal) {
      return ((Principal) principal).getName();
    }
    return String.valueOf(principal);
  }

}
