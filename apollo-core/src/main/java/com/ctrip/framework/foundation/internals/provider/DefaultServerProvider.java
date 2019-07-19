package com.ctrip.framework.foundation.internals.provider;

import com.ctrip.framework.apollo.core.ConfigConsts;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultServerProvider implements ServerProvider {
  private static final Logger logger = LoggerFactory.getLogger(DefaultServerProvider.class);
  private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
  private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
  private static String PROPERTIES_PATH = "";

  private String m_env;
  private String m_dc;

  private Properties m_serverProperties = new Properties();

  @Override
  public void initialize() {
    try {
      String path = Utils.isOSWindows() ? SERVER_PROPERTIES_WINDOWS : SERVER_PROPERTIES_LINUX;
      PROPERTIES_PATH = path;

      File file = new File(path);
      if (file.exists() && file.canRead()) {
        logger.info("Loading {}", file.getAbsolutePath());
        FileInputStream fis = new FileInputStream(file);
        initialize(fis);
        return;
      }
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
              ConfigConsts.APPLICATION_CLASSPATH);
      if (in == null) {
        in = DefaultServerProvider.class.getResourceAsStream(ConfigConsts.APPLICATION_CLASSPATH);
      }
      PROPERTIES_PATH = ConfigConsts.APPLICATION_CLASSPATH;
      initialize(in);
    } catch (Throwable ex) {
      logger.error("Initialize DefaultServerProvider failed.", ex);
    }
  }

  @Override
  public void initialize(InputStream in) {
    try {
      if (in != null) {
        try {
          m_serverProperties.load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
        } finally {
          in.close();
        }
      }

      initEnvType();
      initDataCenter();
    } catch (Throwable ex) {
      logger.error("Initialize DefaultServerProvider failed.", ex);
    }
  }

  @Override
  public String getDataCenter() {
    return m_dc;
  }

  @Override
  public boolean isDataCenterSet() {
    return m_dc != null;
  }

  @Override
  public String getEnvType() {
    return m_env;
  }

  @Override
  public boolean isEnvTypeSet() {
    return m_env != null;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("env".equalsIgnoreCase(name)) {
      String val = getEnvType();
      return val == null ? defaultValue : val;
    } else if ("dc".equalsIgnoreCase(name)) {
      String val = getDataCenter();
      return val == null ? defaultValue : val;
    } else {
      String val = m_serverProperties.getProperty(name, defaultValue);
      return val == null ? defaultValue : val.trim();
    }
  }

  @Override
  public Class<? extends Provider> getType() {
    return ServerProvider.class;
  }

  private void initEnvType() {
    // 1. Try to get environment from JVM system property
    m_env = System.getProperty("env");
    if (!Utils.isBlank(m_env)) {
      m_env = m_env.trim();
      logger.info("Environment is set to [{}] by JVM system property 'env'.", m_env);
      return;
    }

    // 2. Try to get environment from OS environment variable
    m_env = System.getenv("ENV");
    if (!Utils.isBlank(m_env)) {
      m_env = m_env.trim();
      logger.info("Environment is set to [{}] by OS env variable 'ENV'.", m_env);
      return;
    }

    // 3. Try to get environment from file "server.properties"
    m_env = m_serverProperties.getProperty("env");
    if (!Utils.isBlank(m_env)) {
      m_env = m_env.trim();
      logger.info("Environment is set to [{}] by property 'env' in server.properties.", m_env);
      return;
    }

    // 4. Set environment to null.
    m_env = null;
    logger.info("Environment is set to null. Because it is not available in either (1) JVM system property 'env', (2) OS env variable 'ENV' nor (3) property 'env' from the properties InputStream.");
  }

  private void initDataCenter() {
    // 1. Try to get environment from JVM system property
    m_dc = System.getProperty("idc");
    if (!Utils.isBlank(m_dc)) {
      m_dc = m_dc.trim();
      logger.info("Data Center is set to [{}] by JVM system property 'idc'.", m_dc);
      return;
    }

    // 2. Try to get idc from OS environment variable
    m_dc = System.getenv("IDC");
    if (!Utils.isBlank(m_dc)) {
      m_dc = m_dc.trim();
      logger.info("Data Center is set to [{}] by OS env variable 'IDC'.", m_dc);
      return;
    }

    // 3. Try to get idc from from file "server.properties"
    m_dc = m_serverProperties.getProperty("idc");
    if (!Utils.isBlank(m_dc)) {
      m_dc = m_dc.trim();
      logger.info("Data Center is set to [{}] by property 'idc' in {}.", m_dc, PROPERTIES_PATH);
      return;
    }

    // 4. Set Data Center to null.
    m_dc = null;
    logger.debug("Data Center is set to null. Because it is not available in either (1) JVM system property 'idc', (2) OS env variable 'IDC' nor (3) property 'idc' from the properties InputStream.");
  }

  @Override
  public String toString() {
    return "environment [" + getEnvType() + "] data center [" + getDataCenter() + "] properties: " + m_serverProperties
        + " (DefaultServerProvider)";
  }
}
