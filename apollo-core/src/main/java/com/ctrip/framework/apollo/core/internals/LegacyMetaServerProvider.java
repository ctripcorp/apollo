package com.ctrip.framework.apollo.core.internals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;

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
    domains.put(Env.LOCAL,
        env.getProperty("local_meta", prop.getProperty("local.meta")));
    domains.put(Env.DEV,
        env.getProperty("dev_meta", prop.getProperty("dev.meta")));
    domains.put(Env.FAT,
        env.getProperty("fat_meta", prop.getProperty("fat.meta")));
    domains.put(Env.UAT,
        env.getProperty("uat_meta", prop.getProperty("uat.meta")));
    domains.put(Env.LPT,
        env.getProperty("lpt_meta", prop.getProperty("lpt.meta")));
    domains.put(Env.PRO,
        env.getProperty("pro_meta", prop.getProperty("pro.meta")));
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
