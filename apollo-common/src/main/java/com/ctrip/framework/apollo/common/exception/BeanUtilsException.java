package com.ctrip.framework.apollo.common.exception;

/**
 * Bean工具异常
 */
public class BeanUtilsException extends RuntimeException {

  /**
   * 通过异常对象构造 Bean工具异常
   *
   * @param e 具体的异常对象
   */
  public BeanUtilsException(Throwable e) {
    super(e);
  }

}
