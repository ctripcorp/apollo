package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import java.util.Date;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 消费者授权令牌 Repository层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerTokenRepository extends PagingAndSortingRepository<ConsumerToken, Long> {

  /**
   * 通过令牌查找消费者令牌
   *
   * @param token     授权令牌
   * @param validDate 令牌有效的日期
   * @return 消费者授权令牌
   */
  ConsumerToken findTopByTokenAndExpiresAfter(String token, Date validDate);

  /**
   * 通过消费者id查找消费者令牌
   *
   * @param consumerId 消费者id
   * @return 消费者授权令牌
   */
  ConsumerToken findByConsumerId(Long consumerId);
}
