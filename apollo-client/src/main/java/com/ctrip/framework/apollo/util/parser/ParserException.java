package com.ctrip.framework.apollo.util.parser;

/**
 * 解析异常类
 */
public class ParserException extends Exception {

  /**
   * 构建ParserException
   *
   * @param message 解析异常的消息
   */
  public ParserException(String message) {
    super(message);
  }

  /**
   * 构建ParserException
   *
   * @param message 解析异常的消息
   * @param cause   解析异常的原因
   */
  public ParserException(String message, Throwable cause) {
    super(message, cause);
  }
}
