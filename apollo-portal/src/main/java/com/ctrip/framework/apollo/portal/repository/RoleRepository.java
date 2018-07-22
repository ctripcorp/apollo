package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Role;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
  /**
   * find role by role name
   */
  Role findTopByRoleName(String roleName);

  @Modifying
  @Query("UPDATE Role SET IsDeleted=1," +
      "RoleName=CONCAT('DELETED_',RoleName,'_',CURRENT_TIMESTAMP)," +
      "DataChange_LastModifiedBy = ?2 WHERE RoleName LIKE CONCAT('Master+',?1) " +
      "OR RoleName LIKE CONCAT('ModifyNamespace+',?1,'+%') " +
      "OR RoleName LIKE CONCAT('ReleaseNamespace+',?1,'+%')")
  Integer batchDeleteByDeleteApp(String appId, String operator);
}
