package com.ctrip.framework.apollo.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * resource工具类
 */
@Slf4j
public class ResourceUtils {

  private ResourceUtils() {
  }

  /**
   * 默认的文件搜索路径
   */
  private static final String[] DEFAULT_FILE_SEARCH_LOCATIONS = new String[]{"./config/", "./"};

  /**
   * 从指定文件路径加载的配置文件
   *
   * @param configPath 指定文件名称
   * @param defaults   默认的配置文件
   * @return 返回从指定文件路径加载的配置文件
   */
  public static Properties readConfigFile(String configPath, Properties defaults) {
    Properties props = new Properties();
    if (defaults != null) {
      props.putAll(defaults);
    }

    // 将指定文件内容加载到Properties中
    try (InputStream in = loadConfigFileFromDefaultSearchLocations(configPath)) {
      if (in != null) {
        props.load(in);
      }
    } catch (IOException ex) {
      log.warn("Reading config failed: {}", ex.getMessage());
    }

    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (String propertyName : props.stringPropertyNames()) {
        sb.append(propertyName).append('=').append(props.getProperty(propertyName)).append('\n');

      }
      if (sb.length() > 0) {
        log.debug("Reading properties: \n" + sb.toString());
      } else {
        log.warn("No available properties: {}", configPath);
      }
    }
    return props;
  }

  /**
   * 从默认的路径下的指定文件加载配置文件
   *
   * @param configPath 指定文件名称
   * @return 返回配置路径下的指定文件的输入流
   */
  private static InputStream loadConfigFileFromDefaultSearchLocations(String configPath) {
    try {
      // 从默认搜索位置加载
      for (String searchLocation : DEFAULT_FILE_SEARCH_LOCATIONS) {
        File candidate = Paths.get(searchLocation, configPath).toFile();
        //如果指定路径下的文件存在，并且是一个的可读的文件，返回这个文件输入流
        if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
          log.debug("Reading config from resource {}", candidate.getAbsolutePath());
          return new FileInputStream(candidate);
        }
      }

      // 从resource路径读取文件，如果存在直接返回这个文件输入流
      URL url = ClassLoaderUtil.getLoader().getResource(configPath);
      if (url != null) {
        InputStream in = getResourceAsStream(url);
        if (in != null) {
          log.debug("Reading config from resource {}", url.getPath());
          return in;
        }
      }

      // 从user.dir路径下加载指定的配置路径的外部资源，如果存在直接返回这个文件输入流
      File candidate = new File(System.getProperty("user.dir"), configPath);
      if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
        log.debug("Reading config from resource {}", candidate.getAbsolutePath());
        return new FileInputStream(candidate);
      }
    } catch (FileNotFoundException e) {
      //ignore
    }
    return null;
  }

  /**
   * 获取Resouce目录下的指定文件的url的输入流.
   *
   * @param url Resouce目录下的指定文件的url
   * @return 返回Resouce目录下的指定文件的url的输入流
   */
  private static InputStream getResourceAsStream(URL url) {
    try {
      return url != null ? url.openStream() : null;
    } catch (IOException e) {
      return null;
    }
  }
}
