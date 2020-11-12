package com.ctrip.framework.apollo.exceptions;

/**
 * Apollo配置异常
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigException extends RuntimeException {

  /**
   * 通过消息构建Apollo配置异常
   *
   * @param message 消息
   */
  public ApolloConfigException(String message) {
    super(message);
  }

  /**
   * 通过消息和异常构建Apollo配置异常
   *
   * @param message 消息
   * @param cause   异常明细
   */
  public ApolloConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
