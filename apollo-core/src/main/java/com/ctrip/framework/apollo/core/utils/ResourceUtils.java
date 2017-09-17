package com.ctrip.framework.apollo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

public class ResourceUtils {

  private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);
  private static final String[] DEFAULT_FILE_SEARCH_LOCATIONS = new String[]{"./config/", "./"};

  @SuppressWarnings("unchecked")
  public static Properties readConfigFile(String configPath, Properties defaults) {
    Properties props = new Properties();
    if (defaults != null) {
      props.putAll(defaults);
    }

    InputStream in = loadConfigFileFromDefaultSearchLocations(configPath);

    try {
      if (in != null) {
        props.load(in);
      }
    } catch (IOException ex) {
      logger.warn("Reading config failed: {}", ex.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          logger.warn("Close config failed: {}", ex.getMessage());
        }
      }
    }

    if (logger.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (String sropertyName : props.stringPropertyNames()) {
        sb.append(sropertyName).append('=').append(props.getProperty(sropertyName)).append('\n');

      }
      if (sb.length() > 0) {
        logger.debug("Reading properties: \n" + sb.toString());
      } else {
        logger.warn("No available properties");
      }
    }
    return props;
  }

  private static InputStream loadConfigFileFromDefaultSearchLocations(String configPath) {
    try {
      for (String searchLocation : DEFAULT_FILE_SEARCH_LOCATIONS) {
        File candidate = Paths.get(searchLocation, configPath).toFile();
        if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
          logger.debug("Reading config from resource {}", candidate.getAbsolutePath());
          return new FileInputStream(candidate);
        }
      }

      InputStream in = ClassLoaderUtil.getLoader().getResourceAsStream(configPath);

      if (in != null) {
        logger.debug("Reading config from resource {}", ClassLoaderUtil.getLoader().getResource(configPath).getPath());
        return in;
      } else {
        // load outside resource under current user path
        File candidate = new File(System.getProperty("user.dir") + configPath);
        if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
          logger.debug("Reading config from resource {}", candidate.getAbsolutePath());
          return new FileInputStream(candidate);
        }
      }
    } catch (FileNotFoundException e) {
      //ignore
    }
    return null;
  }
}
