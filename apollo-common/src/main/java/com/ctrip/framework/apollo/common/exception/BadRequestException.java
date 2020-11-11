package com.ctrip.framework.apollo.common.exception;


import org.springframework.http.HttpStatus;

/**
 * 错误请求异常
 */
public class BadRequestException extends AbstractApolloHttpException {

  /**
   * 构造错误请求异常
   *
   * @param str 关于异常的具体细节
   */
  public BadRequestException(String str) {
    super(str);
    setHttpStatus(HttpStatus.BAD_REQUEST);
  }
}
