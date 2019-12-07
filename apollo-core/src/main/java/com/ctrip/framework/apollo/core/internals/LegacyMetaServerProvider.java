package com.ctrip.framework.apollo.core.internals;

import com.ctrip.framework.apollo.core.constants.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
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
  private static final Map<String, String> domains = new HashMap<>();

  public LegacyMetaServerProvider() {
    initialize();
  }

  private void initialize() {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);

    domains.put(Env.LOCAL, getMetaServerAddress(prop, "local_meta", "local.meta"));
    domains.put(Env.DEV, getMetaServerAddress(prop, "dev_meta", "dev.meta"));
    domains.put(Env.FAT, getMetaServerAddress(prop, "fat_meta", "fat.meta"));
    domains.put(Env.UAT, getMetaServerAddress(prop, "uat_meta", "uat.meta"));
    domains.put(Env.LPT, getMetaServerAddress(prop, "lpt_meta", "lpt.meta"));
    domains.put(Env.PRO, getMetaServerAddress(prop, "pro_meta", "pro.meta"));


    // find key-value from System Property which key ends with "_meta"
    Map<String, String> metaServerAddressesFromSystemProperty = PropertiesUtil.filterWithKeyEndswith(System.getProperties(), "_meta");
    // remove key's suffix "_meta"
    metaServerAddressesFromSystemProperty = PropertiesUtil.removeKeySuffix(metaServerAddressesFromSystemProperty, "_meta".length());

    // find key-value from OS environment variable which key ends with "_meta"
    Map<String, String> metaServerAddressesFromOSEnvironment = PropertiesUtil.filterWithKeyEndswith(System.getenv(), "_meta");
    // remove key's suffix "_meta"
    metaServerAddressesFromOSEnvironment = PropertiesUtil.removeKeySuffix(metaServerAddressesFromOSEnvironment, "_meta".length());

    // find key-value from properties file which key ends with ".meta"
    Map<String, String> metaServerAddressesFromPropertiesFile = PropertiesUtil.filterWithKeyEndswith(prop, ".meta");
    // remove key's suffix ".meta"
    metaServerAddressesFromPropertiesFile = PropertiesUtil.removeKeySuffix(metaServerAddressesFromPropertiesFile, ".meta".length());

    // begin to add key-value, key is environment, value is meta server address matched
    Map<String, String> metaServerAddresses = new HashMap<>();
    // low priority add first
    metaServerAddresses.putAll(metaServerAddressesFromPropertiesFile);
    metaServerAddresses.putAll(metaServerAddressesFromOSEnvironment);
    metaServerAddresses.putAll(metaServerAddressesFromSystemProperty);

    // add to domain
    domains.putAll(metaServerAddresses);

  }

  private String getMetaServerAddress(Properties prop, String sourceName, String propName) {
    // 1. Get from System Property.
    String metaAddress = System.getProperty(sourceName);
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case,like DEV_META.
      metaAddress = System.getenv(sourceName.toUpperCase());
    }
    if (Strings.isNullOrEmpty(metaAddress)) {
      // 3. Get from properties file.
      metaAddress = prop.getProperty(propName);
    }
    return metaAddress;
  }

  @Override
  public String getMetaServerAddress(String targetEnv) {
    String metaServerAddress = domains.get(targetEnv);
    return metaServerAddress == null ? null : metaServerAddress.trim();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
