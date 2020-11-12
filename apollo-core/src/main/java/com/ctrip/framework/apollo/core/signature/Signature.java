package com.ctrip.framework.apollo.core.signature;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * 签名
 *
 * @author nisiyong
 */
public class Signature {

  /**
   * 认证格式，例如 Authorization=Apollo {appId}:{sign}
   */
  private static final String AUTHORIZATION_FORMAT = "Apollo %s:%s";
  /**
   * 分隔符
   */
  private static final String DELIMITER = "\n";
  /**
   * http请求中head时间戳（header里面加时间戳）
   */
  public static final String HTTP_HEADER_TIMESTAMP = "Timestamp";

  /**
   * 签名
   *
   * @param timestamp     时间戳
   * @param pathWithQuery 查询路径
   * @param secret        密钥
   * @return 签名
   */
  public static String signature(String timestamp, String pathWithQuery, String secret) {
    String stringToSign = timestamp + DELIMITER + pathWithQuery;
    // 加密
    return HmacSha1Utils.signString(stringToSign, secret);
  }

  /**
   * 构建http请求header
   *
   * @param url    请求地址
   * @param appId  应用id
   * @param secret 密钥
   * @return http请求的header
   */
  public static Map<String, String> buildHttpHeaders(String url, String appId, String secret) {
    // 当前毫秒数
    long currentTimeMillis = System.currentTimeMillis();
    // 时间戳
    String timestamp = String.valueOf(currentTimeMillis);

    // 查询路径
    String pathWithQuery = url2PathWithQuery(url);
    String signature = signature(timestamp, pathWithQuery, secret);

    // 构建header
    Map<String, String> headers = Maps.newHashMap();
    headers.put(HttpHeaders.AUTHORIZATION, String.format(AUTHORIZATION_FORMAT, appId, signature));
    headers.put(HTTP_HEADER_TIMESTAMP, timestamp);
    return headers;
  }

  /**
   * 查询的Url路径
   *
   * @param urlString url字符串
   * @return 查询路径
   */
  private static String url2PathWithQuery(String urlString) {
    try {
      //拼接url
      URL url = new URL(urlString);
      String path = url.getPath();
      String query = url.getQuery();

      String pathWithQuery = path;
      // 如果有查询值，添加
      if (query != null && query.length() > 0) {
        pathWithQuery += "?" + query;
      }
      return pathWithQuery;
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid url pattern: " + urlString, e);
    }
  }
}
