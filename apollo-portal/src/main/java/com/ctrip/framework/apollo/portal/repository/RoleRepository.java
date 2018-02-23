package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Role;

import java.util.List;

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
  
  /**
   * find ids by role name
   */
  @Query("select id from Role where (RoleName = CONCAT('ModifyNamespace+', ?1, '+', ?2) or RoleName = CONCAT('ReleaseNamespace+', ?1, '+', ?2)) and IsDeleted = 0")
  List<String> findIdsByRoleName(String appId, String namespaceName);
  
  /**
   * delete role by ids
   */
  @Modifying
  @Query("update Role set isdeleted = 1,DataChange_LastModifiedBy = ?2 where isDeleted = 0 and id in ?1")
  int batchDeleteRole(List<String> ids, String operator);
  
  /**
   * delete userRole by ids
   */
  @Modifying
  @Query("update UserRole set isdeleted = 1,DataChange_LastModifiedBy = ?2 where isDeleted = 0 and roleId in ?1")
  int batchDeleteUserRole(List<String> ids, String operator);
  
  /**
   * delete consumerRole by ids
   */
  @Modifying
  @Query("update ConsumerRole set isdeleted = 1,DataChange_LastModifiedBy = ?2 where isDeleted = 0 and roleId in ?1")
  int batchDeleteConsumerRole(List<String> ids, String operator);
}
