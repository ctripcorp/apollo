package com.ctrip.framework.apollo.core.utils;

import com.google.common.base.Strings;
import java.net.URL;
import java.net.URLDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 类加载器工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class ClassLoaderUtil {

  private ClassLoaderUtil() {
  }

  /**
   * 类加载器.
   */
  @Getter
  private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
  /**
   * 类路径.
   */
  @Getter
  private static String classPath = "";

  static {
    //获取类加载器
    if (loader == null) {
      log.warn("Using system class loader");
      loader = ClassLoader.getSystemClassLoader();
    }

    try {
      URL url = loader.getResource("");
      // 获取类路径
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
      log.warn("Failed to locate class path, fallback to user.dir: {}", classPath, ex);
    }
  }

  /**
   * 指定类是否存在
   *
   * @param className 指定的类名
   * @return 如果类没有找到，返回false,否则，true
   */
  public static boolean isClassPresent(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }
}
