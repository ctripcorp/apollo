package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Role;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
  /**
   * find role by role name
   */
  Role findTopByRoleName(String roleName);

  @Query("select r.id from Role r where r.roleName = CONCAT('Master+', ?1) " +
          " or r.roleName like CONCAT('ModifyNamespace+', ?1, '+%') or" +
          " r.roleName like CONCAT('ReleaseNamespace+',?1, '+%')")
  List<Integer> findByAppId(String appId);
}
