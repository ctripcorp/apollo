package com.ctrip.apollo.biz.repository;


import com.ctrip.apollo.biz.entity.Cluster;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ClusterRepository extends PagingAndSortingRepository<Cluster, Long> {

    List<Cluster> findByAppId(long appId);

}
