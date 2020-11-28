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
