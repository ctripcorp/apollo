package com.ctrip.framework.apollo.util.factory;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.OrderedProperties;
import java.util.Properties;

/**
 * 默认PropertiesFactory实现
 *
 * @author songdragon@zts.io
 */
public class DefaultPropertiesFactory implements PropertiesFactory {

  /**
   * 配置工具类
   */
  private ConfigUtil configUtil;

  /**
   * 构建DefaultPropertiesFactory对象，并注入属性类
   */
  public DefaultPropertiesFactory() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public Properties getPropertiesInstance() {
    // 配置项保持有序，返回有序的Properties对象，否则，返回无序的Properties对象
    if (configUtil.isPropertiesOrderEnabled()) {
      return new OrderedProperties();
    } else {
      return new Properties();
    }
  }
}
