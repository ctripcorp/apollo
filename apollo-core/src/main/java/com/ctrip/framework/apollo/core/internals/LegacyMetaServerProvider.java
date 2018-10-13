package com.ctrip.framework.apollo.core.internals;

import static java.lang.System.getenv;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
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
    domains.put(Env.LOCAL, getenv("local_meta") != null ? getenv("local_meta")
        : env.getProperty("local_meta", prop.getProperty("local.meta")));

    domains.put(Env.DEV, getenv("dev_meta") != null ? getenv("dev_meta")
        : env.getProperty("dev_meta", prop.getProperty("dev.meta")));

    domains.put(Env.FAT, getenv("fat_meta") != null ? getenv("fat_meta")
        : env.getProperty("fat_meta", prop.getProperty("fat.meta")));

    domains.put(Env.UAT, getenv("uat_meta") != null ? getenv("uat_meta")
        : env.getProperty("uat_meta", prop.getProperty("uat.meta")));

    domains.put(Env.LPT, getenv("lpt_meta") != null ? getenv("lpt_meta")
        : env.getProperty("lpt_meta", prop.getProperty("lpt.meta")));

    domains.put(Env.PRO, getenv("pro_meta") != null ? getenv("pro_meta")
        : env.getProperty("pro_meta", prop.getProperty("pro.meta")));
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
