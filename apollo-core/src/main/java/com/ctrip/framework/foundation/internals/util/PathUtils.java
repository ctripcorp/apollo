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

import com.ctrip.framework.foundation.internals.Utils;
import com.google.common.base.Strings;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * resolve path.
 *
 * @author wxq
 */
public class PathUtils {

  public static Path resolve(String keyInSystemProperty, String keyInEnvironment,
      String defaultValueOnLinux, String defaultValueOnWindows) {
    String value = ConfigUtils.getValue(keyInSystemProperty, keyInEnvironment);
    if (!Strings.isNullOrEmpty(value)) {
      return Paths.get(value);
    }

    // default path
    return Utils.isOSWindows() ? Paths.get(defaultValueOnWindows) : Paths.get(defaultValueOnLinux);
  }
}
