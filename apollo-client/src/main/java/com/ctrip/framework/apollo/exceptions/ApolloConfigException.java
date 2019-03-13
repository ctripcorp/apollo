package com.ctrip.framework.apollo.exceptions;

/**
 * 内部封装一个异常，通过这种方法使用和发布信息
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigException extends RuntimeException {
  public ApolloConfigException(String message) {
    super(message);
  }

  public ApolloConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
