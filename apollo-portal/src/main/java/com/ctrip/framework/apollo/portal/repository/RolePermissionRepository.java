package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.RolePermission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 角色权限  Repository
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RolePermissionRepository extends PagingAndSortingRepository<RolePermission, Long> {

  /**
   * 通过角色id列表查询角色权限列表
   *
   * @param roleId 角色id列表
   * @return 符合条件的角色权限列表
   */
  List<RolePermission> findByRoleIdIn(Collection<Long> roleId);

  /**
   * 根据权限id列表批量删除角色权限
   *
   * @param permissionIds 权限id列表
   * @param operator      操作者
   * @return 影响的行数
   */
  @Modifying
  @Query("UPDATE RolePermission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE PermissionId in ?1")
  Integer batchDeleteByPermissionIds(List<Long> permissionIds, String operator);
}
