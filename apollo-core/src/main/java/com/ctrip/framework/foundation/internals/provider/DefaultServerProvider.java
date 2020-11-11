package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的服务器供应器
 */
@Slf4j
public class DefaultServerProvider implements ServerProvider {

  /**
   * Linux的服务器配置文件路径
   */
  private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
  /**
   * Windows的服务器配置文件路径
   */
  private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";

  private String env;
  private String dataCenter;

  private Properties serverProperties = new Properties();

  @Override
  public void initialize() {
    try {
      // 服务器配置文件路径
      String path = Utils.isOSWindows() ? SERVER_PROPERTIES_WINDOWS : SERVER_PROPERTIES_LINUX;

      //获取指定配置文件的输入流初始化应用供应器
      File file = new File(path);
      if (file.exists() && file.canRead()) {
        log.info("Loading {}", file.getAbsolutePath());
        FileInputStream fis = new FileInputStream(file);
        initialize(fis);
        return;
      }

      initialize(null);
    } catch (Throwable ex) {
      log.error("Initialize DefaultServerProvider failed.", ex);
    }
  }

  @Override
  public void initialize(InputStream in) {
    try {
      if (in != null) {
        try {
          serverProperties
              .load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
        } finally {
          in.close();
        }
      }

      initEnvType();
      initDataCenter();
    } catch (Throwable ex) {
      log.error("Initialize DefaultServerProvider failed.", ex);
    }
  }

  @Override
  public String getDataCenter() {
    return dataCenter;
  }

  @Override
  public boolean isDataCenterSet() {
    return dataCenter != null;
  }

  @Override
  public String getEnvType() {
    return env;
  }

  @Override
  public boolean isEnvTypeSet() {
    return env != null;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("env".equalsIgnoreCase(name)) {
      String val = getEnvType();
      return val == null ? defaultValue : val;
    }
    if ("dc".equalsIgnoreCase(name)) {
      String val = getDataCenter();
      return val == null ? defaultValue : val;
    }
    String val = serverProperties.getProperty(name, defaultValue);
    return val == null ? defaultValue : val.trim();
  }

  @Override
  public Class<? extends Provider> getType() {
    return ServerProvider.class;
  }

  /**
   * 初始化环境
   */
  private void initEnvType() {
    // 1. 从系统属性中获取environment
    env = System.getProperty("env");
    if (!Utils.isBlank(env)) {
      env = env.trim();
      log.info("Environment is set to [{}] by JVM system property 'env'.", env);
      return;
    }

    // 2. 从操作系统环境变量获取environment
    env = System.getenv("ENV");
    if (!Utils.isBlank(env)) {
      env = env.trim();
      log.info("Environment is set to [{}] by OS env variable 'ENV'.", env);
      return;
    }

    // 3. 从server.properties获取environment
    env = serverProperties.getProperty("env");
    if (!Utils.isBlank(env)) {
      env = env.trim();
      log.info("Environment is set to [{}] by property 'env' in server.properties.", env);
      return;
    }

    // 默认为空
    env = null;
    log.info(
        "Environment is set to null. Because it is not available in either (1) JVM system property 'env', (2) OS env variable 'ENV' nor (3) property 'env' from the properties InputStream.");
  }

  /**
   * 初始化数据中心
   */
  private void initDataCenter() {
    // 1. 从系统属性中获取idc
    dataCenter = System.getProperty("idc");
    if (!Utils.isBlank(dataCenter)) {
      dataCenter = dataCenter.trim();
      log.info("Data Center is set to [{}] by JVM system property 'idc'.", dataCenter);
      return;
    }

    // 2. 从操作系统环境变量获取IDC
    dataCenter = System.getenv("IDC");
    if (!Utils.isBlank(dataCenter)) {
      dataCenter = dataCenter.trim();
      log.info("Data Center is set to [{}] by OS env variable 'IDC'.", dataCenter);
      return;
    }

    // 3. 从server.properties获取idc
    dataCenter = serverProperties.getProperty("idc");
    if (!Utils.isBlank(dataCenter)) {
      dataCenter = dataCenter.trim();
      log.info("Data Center is set to [{}] by property 'idc' in server.properties.", dataCenter);
      return;
    }

    // 默认为空
    dataCenter = null;
    log.debug(
        "Data Center is set to null. Because it is not available in either (1) JVM system property 'idc', (2) OS env variable 'IDC' nor (3) property 'idc' from the properties InputStream.");
  }

  @Override
  public String toString() {
    return "environment [" + getEnvType() + "] data center [" + getDataCenter() + "] properties: "
        + serverProperties
        + " (DefaultServerProvider)";
  }
}
