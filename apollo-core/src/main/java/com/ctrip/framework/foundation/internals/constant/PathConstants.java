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

package com.ctrip.framework.foundation.internals.constant;

import com.ctrip.framework.foundation.internals.util.PathUtils;
import java.nio.file.Path;

/**
 * @author wang
 */
public interface PathConstants {

  interface ResolvedPaths {

    /**
     * Default value {@code /opt/settings/server.properties} on linux or {@code
     * C:/opt/settings/server.properties} on windows
     */
    Path SERVER_PROPERTIES = PathUtils
        .resolve(SystemPropertyKeys.APOLLO_OPT, EnvironmentKeys.APOLLO_OPT,
            DefaultValuesOnLinux.APOLLO_OPT, DefaultValuesOnWindows.APOLLO_OPT)
        .resolve("settings")
        .resolve("server.properties");
  }

  interface DefaultValuesOnLinux {

    String APOLLO_OPT = "/opt";
  }

  interface DefaultValuesOnWindows {

    String APOLLO_OPT = "C:/opt";
  }

  /**
   * key in {@link System#getProperty(String)}
   */
  interface SystemPropertyKeys {

    String APOLLO_OPT = "APOLLO_OPT";
  }

  /**
   * key in {@link System#getenv(String)}
   */
  interface EnvironmentKeys {

    String APOLLO_OPT = "apollo.opt";
  }

}
