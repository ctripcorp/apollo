package com.ctrip.framework.apollo.common.http;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 富的响应实体
 *
 * @param <T> body类型
 */
@Getter
public class RichResponseEntity<T> {

  /**
   * 状态码
   */
  private int code;
  /**
   * 消息
   */
  private Object message;
  /**
   * 消息体
   */
  private T body;

  /**
   * 200状态码的响应构造
   *
   * @param body 消息体
   * @param <T>  泛型
   * @return 响应实体对象
   */
  public static <T> RichResponseEntity<T> ok(T body) {
    RichResponseEntity<T> richResponseEntity = new RichResponseEntity<>();
    richResponseEntity.message = HttpStatus.OK.getReasonPhrase();
    richResponseEntity.code = HttpStatus.OK.value();
    richResponseEntity.body = body;
    return richResponseEntity;
  }

  /**
   * 200状态码的响应构造
   *
   * @param httpCode 状态码
   * @param message  消息体
   * @param <T>      泛型
   * @return 响应实体对象
   */
  public static <T> RichResponseEntity<T> error(HttpStatus httpCode, Object message) {
    RichResponseEntity<T> richResponseEntity = new RichResponseEntity<>();
    richResponseEntity.message = message;
    richResponseEntity.code = httpCode.value();
    return richResponseEntity;
  }
}
