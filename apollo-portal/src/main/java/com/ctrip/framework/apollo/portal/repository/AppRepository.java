package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.common.entity.App;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;


public interface AppRepository extends PagingAndSortingRepository<App, Long> {

  App findByAppId(String appId);

  List<App> findByOwnerName(String ownerName, Pageable page);

  List<App> findByAppIdIn(Set<String> appIds);

  @Modifying
  @Query("UPDATE App SET IsDeleted=1,AppId=?2,DataChange_LastModifiedBy = ?3 WHERE AppId=?1")
  int deleteApp(String oldAppId, String newAppId, String operator);
}
