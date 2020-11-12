package com.ctrip.framework.apollo.util.http;

import java.util.Map;
import lombok.Data;

/**
 * http请求实体
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class HttpRequest {

  /**
   * 请求的URL
   */
  private String url;
  /**
   * 请求的header
   */
  private Map<String, String> headers;
  /**
   * 请求的连接超时时间
   */
  private int connectTimeout;
  /**
   * 请求的读取超时时间
   */
  private Long readTimeout;

  /**
   * 通过url创建http请求实体
   *
   * @param url the url
   */
  public HttpRequest(String url) {
    this.url = url;
    connectTimeout = -1;
    readTimeout = -1L;
  }
}