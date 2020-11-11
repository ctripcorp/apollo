package com.ctrip.framework.apollo.util.http;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.exceptions.ApolloConfigStatusCodeException;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;

/**
 * http工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class HttpUtil {

  private ConfigUtil m_configUtil;
  private static final Gson GSON = new Gson();

  /**
   * 构造HttpUtil.
   */
  public HttpUtil() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  /**
   * 对http请求执行get操作.
   *
   * @param httpRequest  请求对象
   * @param responseType 响应类型
   * @return 响应对象
   * @throws ApolloConfigException 如果发生任何错误或响应代码既不是200也不是304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Class<T> responseType) {
    Function<String, T> convertResponse = input -> GSON.fromJson(input, responseType);

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  /**
   * 对http请求执行get操作.
   *
   * @param httpRequest  请求对象
   * @param responseType 响应类型
   * @return 响应对象
   * @throws ApolloConfigException 如果发生任何错误或响应代码既不是200也不是304
   */
  public <T> HttpResponse<T> doGet(HttpRequest httpRequest, final Type responseType) {
    Function<String, T> convertResponse = input -> GSON.fromJson(input, responseType);

    return doGetWithSerializeFunction(httpRequest, convertResponse);
  }

  /**
   * 使用序列化函数获取
   *
   * @param httpRequest       请求对象
   * @param serializeFunction 序列化函数
   * @param <T>               泛型
   * @return 响应结果对象
   */
  private <T> HttpResponse<T> doGetWithSerializeFunction(HttpRequest httpRequest,
      Function<String, T> serializeFunction) {
    // 状态码
    int statusCode;
    try {
      // 打开连接
      HttpURLConnection conn = (HttpURLConnection) new URL(httpRequest.getUrl()).openConnection();

      // 请求类型
      conn.setRequestMethod("GET");

      // 设置header
      Map<String, String> headers = httpRequest.getHeaders();
      if (MapUtils.isNotEmpty(headers)) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }

      // 连接超时时间
      int connectTimeout = httpRequest.getConnectTimeout();
      if (connectTimeout < 0) {
        connectTimeout = m_configUtil.getConnectTimeout();
      }

      // 写入超时时间
      int readTimeout = httpRequest.getReadTimeout().intValue();
      if (readTimeout < 0) {
        readTimeout = m_configUtil.getReadTimeout();
      }

      conn.setConnectTimeout(connectTimeout);
      conn.setReadTimeout(readTimeout);

      conn.connect();

      // 状态码
      statusCode = conn.getResponseCode();
      String response;

      try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(),
          StandardCharsets.UTF_8)) {
        response = CharStreams.toString(isr);
      } catch (IOException ex) {
        /**
         * 根据https://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html，我们应该通过读取响应体来清理连接，以便可以重用该连接
         */
        InputStream errorStream = conn.getErrorStream();

        if (errorStream != null) {
          try (InputStreamReader esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8)) {
            CharStreams.toString(esr);
          } catch (IOException ioe) {
            //ignore
          }
        }

        // 200和304不应该触发IOException，因此我们必须抛出原始异常
        if (statusCode == 200 || statusCode == 304) {
          throw ex;
        }
        //对于像404这样的状态代码，调用时需要IOException连接getInputStream()
        throw new ApolloConfigStatusCodeException(statusCode, ex);
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
    }
    throw new ApolloConfigStatusCodeException(statusCode,
        String.format("Get operation failed for %s", httpRequest.getUrl()));
  }

}
