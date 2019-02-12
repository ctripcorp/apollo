package com.ctrip.framework.apollo;

import java.util.Properties;

/**
 * Config files that are properties compatible, e.g. yaml
 *
 * @since 1.3.0
 */
public interface PropertiesCompatibleConfigFile extends ConfigFile {

  /**
   * @return the properties form of the config file
   *
   * @throws RuntimeException if the content could not be transformed to properties
   */
  Properties asProperties();
}
