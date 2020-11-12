package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;

/**
 * 用户信息持有器
 * <p>获取用户的信息，不同的公司应该有不同的实现方式
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserInfoHolder {

  /**
   * 获取用户信息
   *
   * @return 用户信息, 其实只包含了个用户名
   */
  UserInfo getUser();
}