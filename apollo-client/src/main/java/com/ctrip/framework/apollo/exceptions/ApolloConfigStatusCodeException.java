package com.ctrip.framework.apollo.exceptions;

import lombok.Getter;

/**
 * Apollo配置状态码异常
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Getter
public class ApolloConfigStatusCodeException extends RuntimeException {

  /**
   * 状态码
   */
  private final int statusCode;

  /**
   * 通过消息构建Apollo配置异常
   *
   * @param statusCode 状态码
   * @param message    消息
   */
  public ApolloConfigStatusCodeException(int statusCode, String message) {
    super(String.format("[status code: %d] %s", statusCode, message));
    this.statusCode = statusCode;
  }

  /**
   * 通过消息和异常构建Apollo配置异常
   *
   * @param statusCode 状态码
   * @param cause      异常明细
   */
  public ApolloConfigStatusCodeException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
  }
}