package com.ctrip.framework.apollo.util.http;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class HttpUtil {
  private ConfigUtil m_configUtil;
  private Gson gson;
  private static String cookieVal;
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
      setAuth(conn,httpRequest);
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
      if (!StringUtils.isEmpty(cookieVal)) {
        conn.setRequestProperty("Cookie", cookieVal);
      }
      conn.connect();

      statusCode = conn.getResponseCode();
      String response;

      try {
        isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        String cookie = conn.getHeaderField("Set-Cookie");
        if(!StringUtils.isEmpty(cookie)){
          cookieVal = cookie;
        }
        response = CharStreams.toString(isr);
      } catch (IOException ex) {
        /**
         * according to https://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html,
         * we should clean up the connection by reading the response body so that the connection
         * could be reused.
         */
        InputStream errorStream = conn.getErrorStream();

        if (errorStream != null) {
          esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
          try {
            CharStreams.toString(esr);
          } catch (IOException ioe) {
            //ignore
          }
        }

        // 200 and 304 should not trigger IOException, thus we must throw the original exception out
        if (statusCode == 200 || statusCode == 304) {
          throw ex;
        } else {
          // for status codes like 404, IOException is expected when calling conn.getInputStream()
          throw new ApolloConfigStatusCodeException(statusCode, ex);
        }
      }

      if (statusCode == 200) {
        return new HttpResponse<>(statusCode, serializeFunction.apply(response));
      }

      if (statusCode == 304) {
        return new HttpResponse<>(statusCode, null);
      }
    } catch (ApolloConfigStatusCodeException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new ApolloConfigException("Could not complete get operation", ex);
    } finally {
      if (isr != null) {
        try {
          isr.close();
        } catch (IOException ex) {
          // ignore
        }
      }

      if (esr != null) {
        try {
          esr.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }

    throw new ApolloConfigStatusCodeException(statusCode,
        String.format("Get operation failed for %s", httpRequest.getUrl()));
  }

  /**
   * 简单添加Authorization授权登录
   * @param conn
   * @param httpRequest
   */
  private void setAuth(HttpURLConnection conn,HttpRequest httpRequest){
    String url = httpRequest.getUrl();
    if(StringUtils.isEmpty(url)){
      return;
    }
    int b = url.indexOf("@");
    if(b<=0){
      return;
    }
    int a = url.indexOf("//");
    if(a<=0||a+2>b){
      return;
    }
    String up = url.substring(a+2,b);
    if(StringUtils.isEmpty(up)){
      return;
    }
    String[] sts = StringUtils.split(up,":");
    if(sts==null||sts.length < 2){
      return;
    }
    try {
      String username = URLDecoder.decode(sts[0], "UTF-8");
      String password = URLDecoder.decode(sts[1], "UTF-8");
      String input = username + ":" + password;
      //String encoding = java.util.Base64.getEncoder().encodeToString(input.getBytes());
      String encoding = new sun.misc.BASE64Encoder().encode(input.getBytes());
      conn.setRequestProperty("Authorization", "Basic " + encoding);
    } catch (UnsupportedEncodingException e) {
      throw new ApolloConfigStatusCodeException(401,
              String.format("Get Authorization failed for %s", httpRequest.getUrl()));
    }
  }

}
