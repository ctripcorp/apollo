package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 配置服务自身配置  Repository层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfig, Long> {

  /**
   * 通过配置项Key、配置对应的集群查询配置服务自身配置信息
   *
   * @param key     配置项Key
   * @param cluster 配置对应的集群
   * @return 符合条件的配置服务自身配置信息
   */
  ServerConfig findTopByKeyAndCluster(String key, String cluster);
}
