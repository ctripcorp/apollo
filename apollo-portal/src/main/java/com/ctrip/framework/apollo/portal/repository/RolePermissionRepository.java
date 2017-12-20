package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.RolePermission;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RolePermissionRepository extends PagingAndSortingRepository<RolePermission, Long> {

  /**
   * find role permissions by role ids
   */
  List<RolePermission> findByRoleIdIn(Collection<Long> roleId);

  @Modifying
  @Query(value = "update RolePermission set IsDeleted = 1 where PermissionId in ?1",nativeQuery = true)
  int deleteById(List<Integer> ids);
}
