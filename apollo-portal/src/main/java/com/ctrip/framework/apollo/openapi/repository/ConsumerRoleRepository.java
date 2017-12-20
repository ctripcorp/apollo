package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerRoleRepository extends PagingAndSortingRepository<ConsumerRole, Long> {
  /**
   * find consumer roles by userId
   *
   * @param consumerId consumer id
   */
  List<ConsumerRole> findByConsumerId(long consumerId);

  /**
   * find consumer roles by roleId
   */
  List<ConsumerRole> findByRoleId(long roleId);

  ConsumerRole findByConsumerIdAndRoleId(long consumerId, long roleId);

  @Modifying
  @Query(value = "update ConsumerRole set IsDeleted = 1 where RoleId in ?1",nativeQuery = true)
  int deleteByRoleId(List<Integer> roleIds);
}
