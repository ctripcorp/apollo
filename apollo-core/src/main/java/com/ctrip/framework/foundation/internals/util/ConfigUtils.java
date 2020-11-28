/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
   * @param keyInEnvironment    the name of the environment variable
   * @return the string value of the variable, or <code>null</code> if the variable is not defined
   * in the system property or the system environment
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
