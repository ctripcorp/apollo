package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Namespace;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface NamespaceRepository extends PagingAndSortingRepository<Namespace, Long> {

  List<Namespace> findByAppIdAndClusterNameOrderByIdAsc(String appId, String clusterName);

  Namespace findByAppIdAndClusterNameAndNamespaceName(String appId, String clusterName, String namespaceName);

  @Modifying
  @Query("update Namespace set isdeleted=1,DataChange_LastModifiedBy = ?3 where appId=?1 and clusterName=?2")
  int batchDelete(String appId, String clusterName, String operator);

  List<Namespace> findByAppIdAndNamespaceName(String appId, String namespaceName);

  List<Namespace> findByNamespaceName(String namespaceName, Pageable page);

  int countByNamespaceNameAndAppIdNot(String namespaceName, String appId);

  @Modifying
  @Query("UPDATE Namespace SET IsDeleted=1,AppId=?2,DataChange_LastModifiedBy = ?3 WHERE AppId=?1")
  int batchDeleteByDeleteApp(String oldAppId, String newAppId, String operator);

  int countByAppId(String appId);
}
