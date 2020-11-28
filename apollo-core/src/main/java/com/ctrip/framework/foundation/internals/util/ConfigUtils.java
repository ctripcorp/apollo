package com.ctrip.framework.foundation.internals.util;

import com.google.common.base.Strings;

/**
 * resolve the config.
 *
 * @author wxq
 */
public class ConfigUtils {

  /**
   * Gets the value indicated by the specified key.
   *
   * @param keyInSystemProperty the name of the system property
   * @param keyInEnvironment the name of the environment variable
   * @return the string value of the variable, or <code>null</code>
   *         if the variable is not defined in the system property or the system environment
   */
  public static String getValue(String keyInSystemProperty, String keyInEnvironment) {
    // Get from System Property
    final String valueInSystemProperty = System.getProperty(keyInSystemProperty);
    if (!Strings.isNullOrEmpty(valueInSystemProperty)) {
      // return if value exists
      return valueInSystemProperty;
    }

    // Get from OS environment variable
    final String valueInEnvironment = System.getenv(keyInEnvironment);
    if (!Strings.isNullOrEmpty(valueInEnvironment)) {
      // return if value exists
      return valueInEnvironment;
    }

    return null;
  }
}
