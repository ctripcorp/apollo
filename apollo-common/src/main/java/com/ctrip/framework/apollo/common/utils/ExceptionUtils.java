package com.ctrip.framework.apollo.common.utils;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 异常工具类
 */
public final class ExceptionUtils {

  private static Gson gson = new Gson();
  /**
   * Map类型
   */
  private static Type mapType = new TypeToken<Map<String, Object>>() {
  }.getType();

  /**
   * 对请求状态码异常的字符串
   *
   * @param e 请求状态码异常
   * @return 对请求状态码异常的字符串
   */
  public static String toString(HttpStatusCodeException e) {
    Map<String, Object> errorAttributes = gson.fromJson(e.getResponseBodyAsString(), mapType);
    if (errorAttributes != null) {
      return MoreObjects.toStringHelper(HttpStatusCodeException.class).omitNullValues()
          .add("status", errorAttributes.get("status"))
          .add("message", errorAttributes.get("message"))
          .add("timestamp", errorAttributes.get("timestamp"))
          .add("exception", errorAttributes.get("exception"))
          .add("errorCode", errorAttributes.get("errorCode"))
          .add("stackTrace", errorAttributes.get("stackTrace")).toString();
    }
    return "";
  }
}
