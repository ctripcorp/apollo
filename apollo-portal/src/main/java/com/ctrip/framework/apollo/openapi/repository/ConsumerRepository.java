package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.Consumer;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 消费者 Repository层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerRepository extends PagingAndSortingRepository<Consumer, Long> {

  /**
   * 通过应用id查询消费者信息
   *
   * @param appId 应用id
   * @return 消费者信息
   */
  Consumer findByAppId(String appId);

}
