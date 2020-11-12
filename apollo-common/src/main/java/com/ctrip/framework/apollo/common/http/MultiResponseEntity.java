package com.ctrip.framework.apollo.common.http;

import java.util.LinkedList;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * 包含多个响应实体的多响应实体类
 */
public class MultiResponseEntity<T> {

  /**
   * 状态码
   */
  private int code;

  /**
   * 实例列表
   */
  private List<RichResponseEntity<T>> entities = new LinkedList<>();

  /**
   * 构建一个MultiResponseEntity
   *
   * @param httpCode 状态码
   */
  private MultiResponseEntity(HttpStatus httpCode) {
    this.code = httpCode.value();
  }

  /**
   * 获取多响应实体实例
   *
   * @param statusCode 状态码
   * @param <T>        泛型
   * @return 多响应实体实例
   */
  public static <T> MultiResponseEntity<T> instance(HttpStatus statusCode) {
    return new MultiResponseEntity<>(statusCode);
  }

  /**
   * 正常的多响应实体
   *
   * @param <T> 泛型
   * @return 正常的多响应实体
   */
  public static <T> MultiResponseEntity<T> ok() {
    return new MultiResponseEntity<>(HttpStatus.OK);
  }

  /**
   * 添加响应实体
   *
   * @param responseEntity 响应实体对象
   */
  public void addResponseEntity(RichResponseEntity<T> responseEntity) {
    if (responseEntity == null) {
      throw new IllegalArgumentException("sub response entity can not be null");
    }
    entities.add(responseEntity);
  }

}
