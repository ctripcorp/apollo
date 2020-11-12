package com.ctrip.framework.apollo.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应实体
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@AllArgsConstructor
@Getter
public class HttpResponse<T> {

  /**
   * 状态码
   */
  private final int statusCode;
  /**
   * 返回实体
   */
  private final T body;
}
