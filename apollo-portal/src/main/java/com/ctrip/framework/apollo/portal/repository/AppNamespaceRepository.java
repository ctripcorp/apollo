package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long> {

  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  AppNamespace findByName(String namespaceName);

  AppNamespace findByNameAndIsPublic(String namespaceName, boolean isPublic);

  List<AppNamespace> findByIsPublicTrue();

  @Modifying
  @Query("update AppNamespace set isdeleted=1,dataChange_LastModifiedBy = ?3 where isDeleted = 0 and appId= ?1 and name = ?2 ")
  int batchDelete(String appId, String namespaceName, String operator);
}
