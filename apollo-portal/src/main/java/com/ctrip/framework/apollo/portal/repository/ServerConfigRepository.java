package com.ctrip.framework.apollo.portal.repository;


import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 服务配置存储库.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfig, Long> {

  /**
   * 通过配置项Key找到指定的服务配置信息
   *
   * @param key 配置项Key
   * @return 指定的服务配置信息
   */
  ServerConfig findByKey(String key);
}
