package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.List;

/**
 * 用户服务
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserService {


  /**
   * 通过密码查询用户信息列表
   *
   * @param keyword 密码
   * @param offset  偏移量
   * @param limit   限制量
   * @return 用户信息列表
   */
  List<UserInfo> searchUsers(String keyword, int offset, int limit);

  /**
   * 通过应用id查询用户信息
   *
   * @param userId 用户id
   * @return 指定用户id的用户信息
   */
  UserInfo findByUserId(String userId);

  /**
   * 过应用id集合查询用户信息
   *
   * @param userIds 用户id集合
   * @return 用户信息列表
   */
  List<UserInfo> findByUserIds(List<String> userIds);

}
