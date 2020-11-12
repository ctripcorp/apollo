package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * 默认的应用供应器
 */
@Slf4j
public class DefaultApplicationProvider implements ApplicationProvider {

  /**
   * 应用 properties 配置文件路径
   */
  private static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
  /**
   * 配置文件app.properties文件的属性内容
   */
  private Properties appProperties = new Properties();
  /**
   * 应用的AppId
   */
  private String appId;
  /**
   * 应用的访问密钥
   */
  private String accessKeySecret;

  @Override
  public void initialize() {
    //获取指定配置文件的输入流初始化应用供应器
    try {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(APP_PROPERTIES_CLASSPATH.substring(1));
      if (in == null) {
        in = DefaultApplicationProvider.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
      }

      initialize(in);
    } catch (Throwable ex) {
      log.error("Initialize DefaultApplicationProvider failed.", ex);
    }
  }

  @Override
  public void initialize(InputStream in) {
    try {
      if (in != null) {
        try {
          appProperties
              .load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
        } finally {
          in.close();
        }
      }

      initAppId();
      initAccessKey();
    } catch (Throwable ex) {
      log.error("Initialize DefaultApplicationProvider failed.", ex);
    }
  }

  @Override
  public String getAppId() {
    return appId;
  }

  @Override
  public String getAccessKeySecret() {
    return accessKeySecret;
  }

  @Override
  public boolean isAppIdSet() {
    return StringUtils.isNotBlank(appId);
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("app.id".equals(name)) {
      String val = getAppId();
      return val == null ? defaultValue : val;
    }

    if ("apollo.accesskey.secret".equals(name)) {
      String val = getAccessKeySecret();
      return val == null ? defaultValue : val;
    }

    String val = appProperties.getProperty(name, defaultValue);
    return val == null ? defaultValue : val;
  }

  @Override
  public Class<? extends Provider> getType() {
    return ApplicationProvider.class;
  }

  /**
   * 初始化AppId
   */
  private void initAppId() {
    // 1. 从系统属性中获取app.id
    appId = System.getProperty("app.id");
    if (StringUtils.isNotBlank(appId)) {
      appId = appId.trim();
      log.info("App ID is set to {} by app.id property from System Property", appId);
      return;
    }

    // 2. 从操作系统环境变量获取app id
    appId = System.getenv("APP_ID");
    if (StringUtils.isNotBlank(appId)) {
      appId = appId.trim();
      log.info("App ID is set to {} by APP_ID property from OS environment variable", appId);
      return;
    }

    // 3. 从app.properties获取app.id
    appId = appProperties.getProperty("app.id");
    if (StringUtils.isNotBlank(appId)) {
      appId = appId.trim();
      log.info("App ID is set to {} by app.id property from {}", appId,
          APP_PROPERTIES_CLASSPATH);
      return;
    }
    // 默认为空
    appId = null;
    log.warn("app.id is not available from System Property and {}. It is set to null",
        APP_PROPERTIES_CLASSPATH);
  }

  /**
   * 初始化accessKeySecret
   */
  private void initAccessKey() {
    // 1. 从系统属性中获取accesskey secret
    accessKeySecret = System.getProperty("apollo.accesskey.secret");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      log
          .info("ACCESSKEY SECRET is set by apollo.accesskey.secret property from System Property");
      return;
    }

    // 2. 从操作系统环境变量获取accesskey secret
    accessKeySecret = System.getenv("APOLLO_ACCESSKEY_SECRET");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      log.info(
          "ACCESSKEY SECRET is set by APOLLO_ACCESSKEY_SECRET property from OS environment variable");
      return;
    }

    // 3. 从app.properties获取accesskey secret
    accessKeySecret = appProperties.getProperty("apollo.accesskey.secret");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      log.info("ACCESSKEY SECRET is set by apollo.accesskey.secret property from {}",
          APP_PROPERTIES_CLASSPATH);
      return;
    }
    // 默认为空
    accessKeySecret = null;
  }

  @Override
  public String toString() {
    return "appId [" + getAppId() + "] properties: " + appProperties
        + " (DefaultApplicationProvider)";
  }
}
