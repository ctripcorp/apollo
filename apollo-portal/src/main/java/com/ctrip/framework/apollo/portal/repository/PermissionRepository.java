package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Permission;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PermissionRepository extends PagingAndSortingRepository<Permission, Long> {
  /**
   * find permission by permission type and targetId
   */
  Permission findTopByPermissionTypeAndTargetId(String permissionType, String targetId);

  /**
   * find permissions by permission types and targetId
   */
  List<Permission> findByPermissionTypeInAndTargetId(Collection<String> permissionTypes,
                                                     String targetId);

  @Query("select p.id from Permission p where p.targetId = ?1 or p.targetId like CONCAT(?1, '+%')")
  List<Integer> findPermissionId(String appId);

  @Modifying
  @Query(value = "update Permission set IsDeleted = 1 where Id in ?1",nativeQuery = true)
  int deleteById(List<Integer> ids);

}
