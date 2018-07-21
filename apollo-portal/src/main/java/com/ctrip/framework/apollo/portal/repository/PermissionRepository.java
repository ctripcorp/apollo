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
  
  /**
   * find permissionsId by targetId
   */
  @Query("select id from Permission where targetId = CONCAT(?1, '+', ?2)  and isDeleted = 0")
  List<String> findListByTargetId(String appId, String namespaceName);
  
  /**
   * delete permissions by ids
   */
  @Modifying
  @Query("update Permission set isdeleted = 1,DataChange_LastModifiedBy = ?2 where isDeleted = 0 and id in ?1")
  int batchDelete(List<String> ids, String operator);
}
