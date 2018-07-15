package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;


public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long>{

  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  List<AppNamespace> findByAppIdAndNameIn(String appId, Set<String> namespaceNames);

  AppNamespace findByNameAndIsPublicTrue(String namespaceName);

  List<AppNamespace> findByNameInAndIsPublicTrue(Set<String> namespaceNames);

  List<AppNamespace> findByAppIdAndIsPublic(String appId, boolean isPublic);

  List<AppNamespace> findByAppId(String appId);

  List<AppNamespace> findFirst500ByIdGreaterThanOrderByIdAsc(long id);

  @Modifying
  @Query("UPDATE AppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int batchDeleteByDeleteApp(String appId, String operator);

  int countByAppId(String appId);
}
