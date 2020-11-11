package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.UserPO;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 用户 Repository
 *
 * @author lepdou 2017-04-08
 */
public interface UserRepository extends PagingAndSortingRepository<UserPO, Long> {

  /**
   * 查询前20个有效的用户信息
   *
   * @param enabled 是否有效
   * @return 符合条件的用户信息列表
   */
  List<UserPO> findFirst20ByEnabled(int enabled);

  /**
   * 通过用户名模糊查询有效的用户信息列表
   *
   * @param username 用户名
   * @param enabled  是否有效
   * @return
   */
  List<UserPO> findByUsernameLikeAndEnabled(String username, int enabled);

  /**
   * 通过用户名查询用户信息
   *
   * @param username 用户名
   * @return 指定用户名的用户信息
   */
  UserPO findByUsername(String username);

  /**
   * 通过用户名列表查询用户信息列表
   *
   * @param userNames 用户名列表
   * @return 符合条件的用户信息列表
   */
  List<UserPO> findByUsernameIn(List<String> userNames);
}
