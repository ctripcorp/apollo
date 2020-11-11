package com.ctrip.framework.apollo.openapi.client.exception;

import lombok.Getter;

/**
 * Apollo 开放API 异常
 */
public class ApolloOpenApiException extends RuntimeException {

  /**
   * 状态
   */
  @Getter
  private int status;

  /**
   * 构造ApolloOpenApiException
   *
   * @param status  状态
   * @param reason  原因
   * @param message 消息
   */
  public ApolloOpenApiException(int status, String reason, String message) {
    super(String
        .format("Request to apollo open api failed, status code: %d, reason: %s, message: %s",
            status, reason,
            message));
    this.status = status;
  }
}
