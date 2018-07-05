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
   * delete Permission when delete app.
   */
  @Modifying
  @Query("UPDATE Permission SET IsDeleted=1," +
      "TargetId=CONCAT('DELETED_',TargetId,'_',CURRENT_TIMESTAMP)," +
      "DataChange_LastModifiedBy = ?2 WHERE TargetId LIKE ?1 OR TargetId LIKE CONCAT(?1,'+%')")
  Integer batchDeleteByDeleteApp(String appId, String operator);
}
