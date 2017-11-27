package com.ctrip.framework.apollo.util.http;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class HttpUtil {
  private ConfigUtil m_configUtil;
  private Gson gson;

  /**
   * Constructor.
   */
  public HttpUtil() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    gson = new Gson();
  }

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ApolloConfigException if any error happened or response code is neither 200 nor 304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Class<T> responseType) {
    Function<String, T> convertResponse = new Function<String, T>() {
      @Override
      public T apply(String input) {
        return gson.fromJson(input, responseType);
      }
    };

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  /**
   * Do get operation for the http request.
   *
   * @param httpRequest  the request
   * @param responseType the response type
   * @return the response
   * @throws ApolloConfigException if any error happened or response code is neither 200 nor 304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Type responseType) {
    Function<String, T> convertResponse = new Function<String, T>() {
      @Override
      public T apply(String input) {
        return gson.fromJson(input, responseType);
      }
    };

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  private <T> HttpResponse<T> doGetWithSerializeFunction(HttpRequest httpRequest,
                                                         Function<String, T> serializeFunction) {
    InputStreamReader isr = null;
    InputStreamReader esr = null;
    int statusCode;
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(httpRequest.getUrl()).openConnection();

      conn.setRequestMethod("GET");

      int connectTimeout = httpRequest.getConnectTimeout();
      if (connectTimeout < 0) {
        connectTimeout = m_configUtil.getConnectTimeout();
      }

      int readTimeout = httpRequest.getReadTimeout();
      if (readTimeout < 0) {
        readTimeout = m_configUtil.getReadTimeout();
      }

      conn.setConnectTimeout(connectTimeout);
      conn.setReadTimeout(readTimeout);

      conn.connect();

      statusCode = conn.getResponseCode();
      try {
        isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        esr = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
      } catch (Exception e) {
        // ignore
      }

      if (statusCode == 200) {
        String content = CharStreams.toString(isr);
        return new HttpResponse<>(statusCode, serializeFunction.apply(content));
      }

      if (statusCode == 304) {
        return new HttpResponse<>(statusCode, null);
      }
    } catch (Throwable ex) {
      throw new ApolloConfigException("Could not complete get operation", ex);
    } finally {
      if (isr != null) {
        try {
          CharStreams.toString(isr);
          isr.close();
        } catch (IOException e) {
          // ignore
        }
      }

      if (esr != null) {
          try {
              CharStreams.toString(esr);
              esr.close();
          } catch (Exception e) {
              // ignore
          }
      }
    }

    throw new ApolloConfigStatusCodeException(statusCode,
        String.format("Get operation failed for %s", httpRequest.getUrl()));
  }

}
