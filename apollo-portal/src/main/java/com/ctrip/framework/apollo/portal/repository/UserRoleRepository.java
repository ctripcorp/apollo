package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.UserRole;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 用户角色 - 存储库
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {

  /**
   * 根据用户id找到用户角色
   *
   * @param userId 用户id
   * @return 指定用户的角色列表
   */
  List<UserRole> findByUserId(String userId);

  /**
   * 通过角色id查询用户角色信息
   *
   * @param roleId 角色id
   * @return 指定角色id下的用户角色信息
   */
  List<UserRole> findByRoleId(long roleId);

  /**
   * 根据用户id列表和角色id查询用户角色列表
   *
   * @param userId 用户id列表
   * @param roleId 角色id
   * @return 指定用户id列表和角色id的用户角色列表
   */
  List<UserRole> findByUserIdInAndRoleId(Collection<String> userId, long roleId);

  /**
   * 根据角色id删除用户角色
   *
   * @param roleIds  角色id列表
   * @param operator 操作人
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE UserRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1")
  Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);

}
