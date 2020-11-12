package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * 默认的用户服务
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultUserService implements UserService {

  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    return Collections.singletonList(assembleDefaultUser());
  }

  @Override
  public UserInfo findByUserId(String userId) {
    if (userId.equals("apollo")) {
      return assembleDefaultUser();
    }
    return null;
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    if (userIds.contains("apollo")) {
      return Lists.newArrayList(assembleDefaultUser());
    }
    return null;
  }

  /**
   * 组装默认的用户
   *
   * @return 默认的用户信息
   */
  private UserInfo assembleDefaultUser() {
    UserInfo defaultUser = new UserInfo();
    defaultUser.setUserId("apollo");
    defaultUser.setName("apollo");
    defaultUser.setEmail("apollo@acme.com");

    return defaultUser;
  }
}
