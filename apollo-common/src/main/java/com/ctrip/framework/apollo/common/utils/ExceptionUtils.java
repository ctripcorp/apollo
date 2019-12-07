package com.ctrip.framework.apollo.common.utils;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.client.HttpStatusCodeException;

import java.lang.reflect.Type;
import java.util.Map;

public final class ExceptionUtils {

  private static Gson gson = new Gson();

  private static Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

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
