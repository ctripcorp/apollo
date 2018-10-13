package com.ctrip.framework.apollo.core.internals;

import static java.lang.System.getenv;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * For legacy meta server configuration use, i.e. apollo-env.properties
 */
public class LegacyMetaServerProvider implements MetaServerProvider {

  // make it as lowest as possible, yet not the lowest
  public static final int ORDER = MetaServerProvider.LOWEST_PRECEDENCE - 1;
  private static final Map<Env, String> domains = new HashMap<>();

  public LegacyMetaServerProvider() {
    initialize();
  }

  private void initialize() {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);
    Properties env = System.getProperties();

    domains.put(Env.LOCAL, getMeteServerConf(env, prop, "local_meta", "local.meta"));
    domains.put(Env.DEV, getMeteServerConf(env, prop, "dev_meta", "dev.meta"));
    domains.put(Env.FAT, getMeteServerConf(env, prop, "fat_meta", "fat.meta"));
    domains.put(Env.UAT, getMeteServerConf(env, prop, "uat_meta", "uat.meta"));
    domains.put(Env.LPT, getMeteServerConf(env, prop, "lpt_meta", "lpt.meta"));
    domains.put(Env.PRO, getMeteServerConf(env, prop, "pro_meta", "pro.meta"));
  }

  public String getMeteServerConf(Properties env, Properties prop, String sourceName,
      String propName) {
    // 1. Get from System Property.
    String metaAddress = env.getProperty(sourceName);
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case,like DEV_META.
      metaAddress = getenv(sourceName.toUpperCase());
    }
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 3. Get from properties file.
      metaAddress = prop.getProperty(propName);
    }
    return metaAddress;
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    String metaServerAddress = domains.get(targetEnv);
    return metaServerAddress == null ? null : metaServerAddress.trim();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
