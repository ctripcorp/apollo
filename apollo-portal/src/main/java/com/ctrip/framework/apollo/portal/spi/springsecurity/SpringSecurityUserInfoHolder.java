package com.ctrip.framework.apollo.portal.spi.springsecurity;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.security.Principal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SpringSecurity安全用户信息持有器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class SpringSecurityUserInfoHolder implements UserInfoHolder {


  @Override
  public UserInfo getUser() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(getCurrentUsername());
    return userInfo;
  }

  /**
   * 获取当前用户名
   *
   * @return 用户名
   */
  private String getCurrentUsername() {
    //当前身份验证的Principal
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //如果为用户信息，返回当前用户名
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }
    //如果为Principal，返回Principal的名称
    if (principal instanceof Principal) {
      return ((Principal) principal).getName();
    }
    //强制转换为String
    return String.valueOf(principal);
  }
}
