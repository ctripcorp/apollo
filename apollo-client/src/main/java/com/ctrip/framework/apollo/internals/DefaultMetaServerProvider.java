package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMetaServerProvider implements MetaServerProvider {

  public static final int ORDER = 0;
  private static final Logger logger = LoggerFactory.getLogger(DefaultMetaServerProvider.class);

  private final String metaServerAddress;

  public DefaultMetaServerProvider() {
    metaServerAddress = initMetaServerAddress();
  }

  private String initMetaServerAddress() {
    // 1. Get from System Property
    String metaAddress = System.getProperty(ConfigConsts.APOLLO_META_KEY);
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case
      metaAddress = System.getenv("APOLLO_META");
    }
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 3. Get from server.properties
      metaAddress = Foundation.server().getProperty(ConfigConsts.APOLLO_META_KEY, null);
    }
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 4. Get from app.properties
      metaAddress = Foundation.app().getProperty(ConfigConsts.APOLLO_META_KEY, null);
    }

    if (Strings.isNullOrEmpty(metaAddress)) {
      logger.warn("Could not find meta server address, because it is not available in neither (1) JVM system property 'apollo.meta', (2) OS env variable 'APOLLO_META' (3) property 'apollo.meta' from server.properties nor (4) property 'apollo.meta' from app.properties");
    } else {
      metaAddress = metaAddress.trim();
      logger.info("Located meta services from apollo.meta configuration: {}!", metaAddress);
    }

    return metaAddress;
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    //for default meta server provider, we don't care the actual environment
    return metaServerAddress;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
