package com.ctrip.framework.apollo.core;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.NetUtil;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The meta domain will load the meta server from System environment first, if not exist, will load
 * from apollo-env.properties. If neither exists, will load the default meta url.
 * 
 * Currently, apollo supports local/dev/fat/uat/lpt/pro environments.
 */
public class MetaDomainConsts {

  private static Map<Env, Object> domains = new HashMap<>();

  public static final String DEFAULT_META_URL = "http://config.local";

  static {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);
    Properties env = System.getProperties();
    domains.put(Env.LOCAL,
        env.getProperty("local_meta", prop.getProperty("local.meta", DEFAULT_META_URL)));
    domains.put(Env.DEV,
        env.getProperty("dev_meta", prop.getProperty("dev.meta", DEFAULT_META_URL)));
    domains.put(Env.FAT,
        env.getProperty("fat_meta", prop.getProperty("fat.meta", DEFAULT_META_URL)));
    domains.put(Env.UAT,
        env.getProperty("uat_meta", prop.getProperty("uat.meta", DEFAULT_META_URL)));
    domains.put(Env.LPT,
        env.getProperty("lpt_meta", prop.getProperty("lpt.meta", DEFAULT_META_URL)));
    domains.put(Env.PRO,
        env.getProperty("pro_meta", prop.getProperty("pro.meta", DEFAULT_META_URL)));
  }

  public static String getDomain(Env env) {
    String metaAddress = String.valueOf(domains.get(env));
    String validAddress = NetUtil.getValidAddress( metaAddress );
    return validAddress;
  }
}
