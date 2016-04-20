package com.ctrip.apollo.biz.repository;

import com.ctrip.apollo.biz.entity.Commit;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CommitRepository extends PagingAndSortingRepository<Commit, Long> {

  List<Commit> findByAppIdAndClusterNameAndNamespaceName(String appId, String clusterName,
                                                         String namespaceName);

}
