package com.ctrip.framework.apollo.portal.util;

import static org.junit.Assert.*;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigFileUtilsTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  public void toFilename() {
    final String propertiesFilename0 = ConfigFileUtils.toFilename("123", "default", "application", ConfigFileFormat.Properties);
    logger.info("propertiesFilename0 {}", propertiesFilename0);
    assertEquals("123+default+application.properties", propertiesFilename0);

    final String ymlFilename0 = ConfigFileUtils.toFilename("666", "none", "cc.yml", ConfigFileFormat.YML);
    logger.info("ymlFilename0 {}", ymlFilename0);
    assertEquals("666+none+cc.yml", ymlFilename0);
  }
}