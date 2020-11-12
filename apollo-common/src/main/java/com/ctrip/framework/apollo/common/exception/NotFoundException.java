package com.ctrip.framework.apollo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 未找到异常
 */
public class NotFoundException extends AbstractApolloHttpException {

  /**
   * 通过消息明细构造未找到异常
   *
   * @param str 具体的异常消息明细
   */
  public NotFoundException(String str) {
    super(str);
    setHttpStatus(HttpStatus.NOT_FOUND);
  }
}
