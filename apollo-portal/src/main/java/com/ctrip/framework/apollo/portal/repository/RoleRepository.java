package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Role;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 角色 Repository
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {

  /**
   * 通过角色名查询第一个角色信息
   *
   * @param roleName 角色名
   * @return 符合条件的角色信息
   */
  Role findTopByRoleName(String roleName);

  /**
   * 通过应用id获取角色id列表
   *
   * @param appId 应用id
   * @return 符合条件的角色id列表
   */
  @Query("SELECT r.id from Role r where (r.roleName = CONCAT('Master+', ?1) "
      + "OR r.roleName like CONCAT('ModifyNamespace+', ?1, '+%') "
      + "OR r.roleName like CONCAT('ReleaseNamespace+', ?1, '+%')  "
      + "OR r.roleName = CONCAT('ManageAppMaster+', ?1))")
  List<Long> findRoleIdsByAppId(String appId);

  /**
   * 通过应用id和名称空间名称查询角色id列表
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 符合条件的角色id列表
   */
  @Query("SELECT r.id from Role r where (r.roleName = CONCAT('ModifyNamespace+', ?1, '+', ?2) "
      + "OR r.roleName = CONCAT('ReleaseNamespace+', ?1, '+', ?2))")
  List<Long> findRoleIdsByAppIdAndNamespace(String appId, String namespaceName);

  /**
   * 通过id列表批量删除角色信息
   *
   * @param roleIds  角色id列表
   * @param operator 操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE Role SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1")
  Integer batchDelete(List<Long> roleIds, String operator);
}
