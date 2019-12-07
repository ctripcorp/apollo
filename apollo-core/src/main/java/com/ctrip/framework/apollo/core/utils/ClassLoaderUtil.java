package com.ctrip.framework.apollo.core.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ClassLoaderUtil {
  private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

  private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
  private static String classPath = "";

  static {
    if (loader == null) {
      logger.warn("Using system class loader");
      loader = ClassLoader.getSystemClassLoader();
    }

    try {
      URL url = loader.getResource("");
      // get class path
      if (url != null) {
        classPath = url.getPath();
        classPath = URLDecoder.decode(classPath, "utf-8");
      }

      // 如果是jar包内的，则返回当前路径
      if (Strings.isNullOrEmpty(classPath) || classPath.contains(".jar!")) {
        classPath = System.getProperty("user.dir");
      }
    } catch (Throwable ex) {
      classPath = System.getProperty("user.dir");
      logger.warn("Failed to locate class path, fallback to user.dir: {}", classPath, ex);
    }
  }

  public static ClassLoader getLoader() {
    return loader;
  }


  public static String getClassPath() {
    return classPath;
  }

  public static boolean isClassPresent(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }
}
