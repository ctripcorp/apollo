package com.ctrip.framework.apollo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 服务异常
 */
public class ServiceException extends AbstractApolloHttpException {

  /**
   * 通过消息明细构造未找到异常
   *
   * @param str 具体的异常消息明细
   */
  public ServiceException(String str) {
    super(str);
    setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 通过消息明细构造未找到异常
   *
   * @param str 具体的异常消息明细
   * @param e   具体的异常
   */
  public ServiceException(String str, Exception e) {
    super(str, e);
    setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
