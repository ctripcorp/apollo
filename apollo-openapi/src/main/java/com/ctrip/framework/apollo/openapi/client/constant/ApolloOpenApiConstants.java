package com.ctrip.framework.apollo.openapi.client.constant;

/**
 * Apollo开放API常量
 */
public interface ApolloOpenApiConstants {

  /**
   * 默认连接超时时间
   */
  int DEFAULT_CONNECT_TIMEOUT = 1000;
  /**
   * 默认读取超时时间
   */
  int DEFAULT_READ_TIMEOUT = 5000;
  /**
   * 开放API版本前缀
   */
  String OPEN_API_V1_PREFIX = "/openapi/v1";
  /**
   * JSON日期格式
   */
  String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

}
