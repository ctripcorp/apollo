package com.ctrip.framework.apollo.common.utils;


import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

/**
 * 请求前提条件.
 */
public class RequestPrecondition {

  /**
   * 包含空参数的错误提示信息.
   */
  private static String CONTAIN_EMPTY_ARGUMENT = "request payload should not be contain empty.";
  /**
   * 不正确的请求模型.
   */
  private static String ILLEGAL_MODEL = "request model is invalid";

  /**
   * 检查参数数组不为空.
   *
   * @param args 参数数组
   */
  public static void checkArgumentsNotEmpty(String... args) {
    checkArguments(!StringUtils.isContainEmpty(args), CONTAIN_EMPTY_ARGUMENT);
  }

  /**
   * 检查模型.
   *
   * @param valid 是否验证
   */
  public static void checkModel(boolean valid) {
    checkArguments(valid, ILLEGAL_MODEL);
  }

  /**
   * 检查参数.
   *
   * @param expression   表达式
   * @param errorMessage 错误提示信息
   */
  public static void checkArguments(boolean expression, Object errorMessage) {
    if (!expression) {
      throw new BadRequestException(String.valueOf(errorMessage));
    }
  }
}
