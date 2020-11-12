package com.ctrip.framework.apollo.portal.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@NoArgsConstructor
@Data
public class UserInfo {

  /**
   * 用户身份标识(用户名)
   */
  private String userId;
  /**
   * 用户名
   */
  private String name;
  /**
   * 用户邮箱地址
   */
  private String email;


  public UserInfo(String userId) {
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof UserInfo) {

      if (o == this) {
        return true;
      }

      UserInfo anotherUser = (UserInfo) o;
      return userId.equals(anotherUser.userId);
    }
    return false;

  }
}
