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
