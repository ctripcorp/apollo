package com.ctrip.framework.test.tools;

import java.net.URLClassLoader;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/5/21
 */
public class AloneClassLoader extends URLClassLoader {

  private final ClassLoader appClassLoader;

  public AloneClassLoader() {
    super(((URLClassLoader) getSystemClassLoader()).getURLs(),
        Thread.currentThread().getContextClassLoader().getParent());
    appClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    if (name.startsWith("org.junit.") || name.startsWith("junit.")) {
      return appClassLoader.loadClass(name);
    }

    return super.loadClass(name);
  }
}