package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface GrayReleaseRuleRepository extends PagingAndSortingRepository<GrayReleaseRule, Long> {

  GrayReleaseRule findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(String appId, String clusterName,
                                                                                         String namespaceName, String branchName);

  List<GrayReleaseRule> findByAppIdAndClusterNameAndNamespaceName(String appId,
                                                               String clusterName, String namespaceName);

  List<GrayReleaseRule> findFirst500ByIdGreaterThanOrderByIdAsc(Long id);

  @Modifying
  @Query("UPDATE GrayReleaseRule SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int batchDeleteByDeleteApp(String appId, String operator);

  int countByAppId(String appId);
}
