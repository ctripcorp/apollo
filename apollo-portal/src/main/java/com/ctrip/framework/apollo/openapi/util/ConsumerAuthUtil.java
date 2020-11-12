package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * Consumer 认证工具类c
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerAuthUtil {

  /**
   * Request Attribute —— Consumer 编号
   */
  static final String CONSUMER_ID = "ApolloConsumerId";
  private final ConsumerService consumerService;

  public ConsumerAuthUtil(final ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  /**
   * 获得 Token 获得对应的 Consumer 编号
   *
   * @param token Token
   * @return Consumer 编号
   */
  public Long getConsumerId(String token) {
    return consumerService.getConsumerIdByToken(token);
  }

  /**
   * 设置 Consumer 编号到 Request
   *
   * @param request    请求
   * @param consumerId Consumer 编号
   */
  public void storeConsumerId(HttpServletRequest request, Long consumerId) {
    request.setAttribute(CONSUMER_ID, consumerId);
  }

  /**
   * 获得 Consumer 编号从 Request
   *
   * @param request 请求
   * @return Consumer 编号
   */
  public long retrieveConsumerId(HttpServletRequest request) {
    Object value = request.getAttribute(CONSUMER_ID);

    try {
      return Long.parseLong(value.toString());
    } catch (Throwable ex) {
      throw new IllegalStateException("No consumer id!", ex);
    }
  }
}
