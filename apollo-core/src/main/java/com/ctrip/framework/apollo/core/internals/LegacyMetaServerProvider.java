package com.ctrip.framework.apollo.core.internals;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;

/**
 * 传统元服务器配置供应器
 * <p> 用于传统（遗留）的元服务器配置，即apollo-env.properties
 */
public class LegacyMetaServerProvider implements MetaServerProvider {

  /**
   * 优先级，默认为最低的优先级排序
   */
  public static final int ORDER = LOWEST_PRECEDENCE - 1;
  /**
   * 域，服务器配置（env,address）
   */
  private static final Map<Env, String> DOMAINS = new EnumMap<>(Env.class);

  /**
   * 初始化传统元服务器配置供应器
   */
  LegacyMetaServerProvider() {
    initialize();
  }

  private void initialize() {
    //读取apollo-env.properties文件加载服务器地址信息
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);

    //加载环境与元服务器地址
    DOMAINS.put(Env.LOCAL, getMetaServerAddress(prop, "local_meta", "local.meta"));
    DOMAINS.put(Env.DEV, getMetaServerAddress(prop, "dev_meta", "dev.meta"));
    DOMAINS.put(Env.FAT, getMetaServerAddress(prop, "fat_meta", "fat.meta"));
    DOMAINS.put(Env.UAT, getMetaServerAddress(prop, "uat_meta", "uat.meta"));
    DOMAINS.put(Env.LPT, getMetaServerAddress(prop, "lpt_meta", "lpt.meta"));
    DOMAINS.put(Env.PRO, getMetaServerAddress(prop, "pro_meta", "pro.meta"));
  }

  /**
   * 获取元服务器地址
   *
   * @param prop       属性对象
   * @param sourceName 源名称
   * @param propName   属性名称
   * @return 元服务器地址
   */
  private String getMetaServerAddress(Properties prop, String sourceName, String propName) {
    // 1.从系统属性中获取.
    String metaAddress = System.getProperty(sourceName);
    if (StringUtils.isBlank(metaAddress)) {
      // 2. 从操作系统环境变量中获取，该变量不能包含点，并且通常是大写的，如DEV_META。
      metaAddress = System.getenv(sourceName.toUpperCase());
    }
    if (StringUtils.isBlank(metaAddress)) {
      // 3. 从属性文件中获取.
      metaAddress = prop.getProperty(propName);
    }
    return metaAddress;
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    String metaServerAddress = DOMAINS.get(targetEnv);
    return metaServerAddress == null ? null : metaServerAddress.trim();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
