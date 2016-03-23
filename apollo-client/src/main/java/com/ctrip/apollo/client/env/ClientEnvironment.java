package com.ctrip.apollo.client.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.client.constants.Constants;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.utils.StringUtils;

public class ClientEnvironment {

  private static final Logger logger = LoggerFactory.getLogger(ClientEnvironment.class);

  private final static String DEFAULT_FILE = "/apollo.properties";

  private AtomicReference<Env> env = new AtomicReference<Env>();

  private static ClientEnvironment instance = new ClientEnvironment();

  private ClientEnvironment() {

  }

  public static ClientEnvironment getInstance() {
    return instance;
  }

  public Env getEnv() {
    if (env.get() == null) {
      Env resultEnv = Apollo.getEnv();
      Properties apolloProperties = null;
      try {
        apolloProperties = readConfigFile(DEFAULT_FILE, null);
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not read Apollo properties");
      }
      if (apolloProperties != null) {
        String strEnv = apolloProperties.getProperty(Constants.ENV);
        if (!StringUtils.isBlank(strEnv)) {
          resultEnv = Env.valueOf(strEnv.trim().toUpperCase());
        }
      }
      env.compareAndSet(null, resultEnv);
    }

    if (env.get() == null) {
      throw new IllegalArgumentException("Apollo env is not set");
    }

    return env.get();
  }

  public String getMetaServerDomainName() {
    return MetaDomainConsts.getDomain(getEnv());
  }

  @SuppressWarnings("unchecked")
  private Properties readConfigFile(String configPath, Properties defaults) throws IOException {
    InputStream in = this.getClass().getResourceAsStream(configPath);
    logger.info("Reading config from resource {}", configPath);
    if (in == null) {
      // load outside resource under current user path
      Path path = new File(System.getProperty("user.dir") + configPath).toPath();
      if (Files.isReadable(path)) {
        in = new FileInputStream(path.toFile());
        logger.info("Reading config from file {} ", path);
      }
    }
    Properties props = new Properties();
    if (defaults != null) {
      props.putAll(defaults);
    }

    if (in != null) {
      props.load(in);
    }

    StringBuilder sb = new StringBuilder();
    for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e
        .hasMoreElements();) {
      String key = e.nextElement();
      String val = (String) props.getProperty(key);
      sb.append(key).append('=').append(val).append('\n');
    }
    logger.info("Reading properties: \n" + sb.toString());
    return props;
  }
}
