package com.ctrip.framework.apollo.portal.repository;


import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfig, Long> {
  ServerConfig findByKey(String key);
}
