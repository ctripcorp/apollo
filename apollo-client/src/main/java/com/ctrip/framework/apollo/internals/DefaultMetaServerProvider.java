package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.foundation.Foundation;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的元服务器提供者
 */
@Slf4j
public class DefaultMetaServerProvider implements MetaServerProvider {

  /**
   * 排序值.
   */
  public static final int ORDER = 0;
  /**
   * 元服务器地址.
   */
  private final String metaServerAddress;

  /**
   * 初始化默认的元服务器提供者
   */
  public DefaultMetaServerProvider() {
    metaServerAddress = initMetaServerAddress();
  }

  /**
   * 元服务器初始化
   *
   * @return 元服务器地址
   */
  private String initMetaServerAddress() {
    // 1. 从系统属性中获取
    String metaAddress = System.getProperty(ConfigConsts.APOLLO_META_KEY);
    if (StringUtils.isBlank(metaAddress)) {
      // 2. 从操作系统环境变量获取，该变量不能包含点且通常为大写
      metaAddress = System.getenv("APOLLO_META");
    }
    if (StringUtils.isBlank(metaAddress)) {
      // 3.从server.properties获取
      metaAddress = Foundation.server().getProperty(ConfigConsts.APOLLO_META_KEY, null);
    }
    if (StringUtils.isBlank(metaAddress)) {
      // 4. 从app.properties获取
      metaAddress = Foundation.app().getProperty(ConfigConsts.APOLLO_META_KEY, null);
    }

    if (StringUtils.isBlank(metaAddress)) {
      log.warn(
          "Could not find meta server address, because it is not available in neither (1) JVM system property 'apollo.meta', (2) OS env variable 'APOLLO_META' (3) property 'apollo.meta' from server.properties nor (4) property 'apollo.meta' from app.properties");
    } else {
      metaAddress = metaAddress.trim();
      log.info("Located meta services from apollo.meta configuration: {}!", metaAddress);
    }

    return metaAddress;
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    // 对于默认的元服务器供应器，不关心实际的环境
    return metaServerAddress;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
