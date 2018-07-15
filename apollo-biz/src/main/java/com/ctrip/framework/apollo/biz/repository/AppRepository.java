package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.common.entity.App;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppRepository extends PagingAndSortingRepository<App, Long> {

  @Query("SELECT a from App a WHERE a.name LIKE %:name%")
  List<App> findByName(@Param("name") String name);

  App findByAppId(String appId);

  @Modifying
  @Query("UPDATE App SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int deleteApp(String appId, String operator);

  int countByAppId(String appId);
}
