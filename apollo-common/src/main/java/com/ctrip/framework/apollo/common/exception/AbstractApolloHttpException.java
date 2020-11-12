package com.ctrip.framework.apollo.common.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 抽象的apollo Http响应异常
 */
@Data
public abstract class AbstractApolloHttpException extends RuntimeException {

  private static final long serialVersionUID = -1713129594004951820L;
  /**
   * 请求状态
   */
  protected HttpStatus httpStatus;

  /**
   * 通过异常信息构造Http响应异常
   *
   * @param msg 异常的具体细节
   */
  public AbstractApolloHttpException(String msg) {
    super(msg);
  }

  /**
   * 通过异常信息和具体的异常构造Http响应异常
   *
   * @param msg 异常的具体细节
   * @param e   具体的异常
   */
  public AbstractApolloHttpException(String msg, Exception e) {
    super(msg, e);
  }
}
