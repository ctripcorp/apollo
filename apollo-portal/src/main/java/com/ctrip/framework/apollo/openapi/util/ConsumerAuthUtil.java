package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerAuthUtil {
  static final String CONSUMER_ID = "ApolloConsumerId";
  private final ConsumerService consumerService;

  public ConsumerAuthUtil(final ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  public Long getConsumerId(String token) {
    return consumerService.getConsumerIdByToken(token);
  }

  public void storeConsumerId(HttpServletRequest request, Long consumerId) {
    request.setAttribute(CONSUMER_ID, consumerId);
  }

  public long retrieveConsumerId(HttpServletRequest request) {
    Object value = request.getAttribute(CONSUMER_ID);

    try {
      return Long.parseLong(value.toString());
    } catch (Throwable ex) {
      throw new IllegalStateException("No consumer id!", ex);
    }
  }
}
