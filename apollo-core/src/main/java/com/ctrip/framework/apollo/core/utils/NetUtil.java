package com.ctrip.framework.apollo.core.utils;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 网络工具类
 *
 * @author gl49
 * @date 2018/6/8.
 */
public class NetUtil {

  /**
   * 默认的超时时间
   */
  private static final int DEFAULT_TIMEOUT_IN_SECONDS = (int) TimeUnit.SECONDS.toMillis(5);

  /**
   * ping URL
   *
   * @return 如果ping确定，返回true，否则,返回false
   */
  public static boolean pingUrl(String address) {
    try {
      URL urlObj = new URL(address);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod("GET");
      connection.setUseCaches(false);
      connection.setConnectTimeout(DEFAULT_TIMEOUT_IN_SECONDS);
      connection.setReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS);
      int statusCode = connection.getResponseCode();
      cleanUpConnection(connection);
      return (200 <= statusCode && statusCode <= 399);
    } catch (Throwable ignore) {
    }
    return false;
  }

  /**
   * 清理连接
   * <p>
   * 根据https://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html，我们应该通过读取响应体来清理连接，以便可以重用该连接。
   */
  private static void cleanUpConnection(HttpURLConnection conn) {
    try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(),
        StandardCharsets.UTF_8)) {
      CharStreams.toString(isr);
    } catch (IOException e) {
      InputStream errorStream = conn.getErrorStream();
      if (errorStream != null) {
        try (InputStreamReader esr = new InputStreamReader(errorStream, StandardCharsets.UTF_8)) {
          CharStreams.toString(esr);
        } catch (IOException ioe) {
          //ignore
        }
      }
    }
  }
}
