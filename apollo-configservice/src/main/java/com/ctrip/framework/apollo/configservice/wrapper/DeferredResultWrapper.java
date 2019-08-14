package com.ctrip.framework.apollo.configservice.wrapper;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.TimeoutProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DeferredResultWrapper implements TimeoutProvider {
  //private static final long TIMEOUT = 60 * 1000;//60 seconds
  private static final Logger logger = LoggerFactory.getLogger(DeferredResultWrapper.class);
  public static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
  private Properties m_appProperties = new Properties();

  private String m_timeout;

  private static final ResponseEntity<List<ApolloConfigNotification>>
      NOT_MODIFIED_RESPONSE_LIST = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

  private Map<String, String> normalizedNamespaceNameToOriginalNamespaceName;
  private DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> result;


  public DeferredResultWrapper() {
    result = new DeferredResult<>(Long.parseLong(getTimeout()), NOT_MODIFIED_RESPONSE_LIST);
  }

  public void recordNamespaceNameNormalizedResult(String originalNamespaceName, String normalizedNamespaceName) {
    if (normalizedNamespaceNameToOriginalNamespaceName == null) {
      normalizedNamespaceNameToOriginalNamespaceName = Maps.newHashMap();
    }
    normalizedNamespaceNameToOriginalNamespaceName.put(normalizedNamespaceName, originalNamespaceName);
  }


  public void onTimeout(Runnable timeoutCallback) {
    result.onTimeout(timeoutCallback);
  }

  public void onCompletion(Runnable completionCallback) {
    result.onCompletion(completionCallback);
  }


  public void setResult(ApolloConfigNotification notification) {
    setResult(Lists.newArrayList(notification));
  }

  /**
   * The namespace name is used as a key in client side, so we have to return the original one instead of the correct one
   */
  public void setResult(List<ApolloConfigNotification> notifications) {
    if (normalizedNamespaceNameToOriginalNamespaceName != null) {
      notifications.stream().filter(notification -> normalizedNamespaceNameToOriginalNamespaceName.containsKey
          (notification.getNamespaceName())).forEach(notification -> notification.setNamespaceName(
              normalizedNamespaceNameToOriginalNamespaceName.get(notification.getNamespaceName())));
    }

    result.setResult(new ResponseEntity<>(notifications, HttpStatus.OK));
  }

  public DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> getResult() {
    return result;
  }

  @Override
  public void initialize() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES_CLASSPATH);
      if (in == null) {
        in = DeferredResultWrapper.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
      }

      initialize(in);
    } catch (Throwable ex) {
      logger.error("Initialize DeferredResultWrapper failed.", ex);
    }
  }

  @Override
  public void initialize(InputStream in) {
    try {
      if (in != null) {
        try {
          m_appProperties.load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
        } finally {
          in.close();
        }
      }

      initTimeOut();
    } catch (Throwable ex) {
      logger.error("Initialize DeferredResultWrapper failed.", ex);
    }
  }

  @Override
  public String getTimeout() {
    return m_timeout;
  }

  @Override
  public boolean isTimeoutSet() {
    return Utils.isBlank(m_timeout);
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("timeout".equals(name)) {
      String val = getTimeout();
      return val == null ? defaultValue : val;
    } else {
      String val = m_appProperties.getProperty(name, defaultValue);
      return val == null ? defaultValue : val;
    }
  }

  @Override
  public Class<? extends Provider> getType() {return TimeoutProvider.class;}

  private void initTimeOut() {
    // 1.Get timeout from System Property
    m_timeout = System.getProperty("timeout");
    if (!Utils.isBlank(m_timeout)) {
      m_timeout = m_timeout.trim();
      logger.info("timeout is set to {} by timeout property from System Property", m_timeout);
      return;
    }

    // 2.Get timeout from OS environment variable
    m_timeout = System.getenv("timeout");
    if (!Utils.isBlank(m_timeout)) {
      m_timeout = m_timeout.trim();
      logger.info("timeout is set to {} by timeout property from OS environment variable", m_timeout);
      return;
    }

    // 3.Get timeout from app.properties.
    m_timeout = m_appProperties.getProperty("timeout");
    if (!Utils.isBlank(m_timeout)) {
      m_timeout = m_timeout.trim();
      logger.info("timeout is set to {} by timeout property from app.properties.", m_timeout, APP_PROPERTIES_CLASSPATH);
      return;
    }

    m_timeout = null;
    logger.warn("timeout is not available from System Property and {}. It is set to null", APP_PROPERTIES_CLASSPATH);
  }

  @Override
  public String toString() {
    return "timeout [" + getTimeout() + "] properties: " + m_timeout + " (DefaultResultWrapper)";
  }
}
