package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Permission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 权限-存储库
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PermissionRepository extends PagingAndSortingRepository<Permission, Long> {

  /**
   * 通过权限类型与权限对象类型获取权限信息
   *
   * @param permissionType 权限类型
   * @param targetId       权限对象类型
   * @return 权限信息
   */
  Permission findTopByPermissionTypeAndTargetId(String permissionType, String targetId);

  /**
   * 通过权限类型集合与权限对象类型获取权限信息列表
   *
   * @param permissionTypes 权限类型集合
   * @param targetId        权限对象类型
   * @return 权限信息列表
   */
  List<Permission> findByPermissionTypeInAndTargetId(Collection<String> permissionTypes,
      String targetId);

  /**
   * 通过权限对象类型模糊查询权限信息列表
   *
   * @param appId 应用id
   * @return 权限信息列表
   */
  @Query("SELECT p.id from Permission p where p.targetId = ?1 or p.targetId like CONCAT(?1, '+%')")
  List<Long> findPermissionIdsByAppId(String appId);

  /**
   * 通过权限对象类型查询权限信息列表
   *
   * @param appId         应用id
   * @param namespaceName 名称空间的名称
   * @return 权限信息列表
   */
  @Query("SELECT p.id from Permission p where p.targetId = CONCAT(?1, '+', ?2)")
  List<Long> findPermissionIdsByAppIdAndNamespace(String appId, String namespaceName);

  /**
   * 批量删除（通过权限id列表更新权限信息）
   *
   * @param permissionIds 权限id列表
   * @param operator      操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE Permission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1")
  Integer batchDelete(List<Long> permissionIds, String operator);
}
