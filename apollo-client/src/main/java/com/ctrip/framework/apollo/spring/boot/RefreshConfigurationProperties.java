package com.ctrip.framework.apollo.spring.boot;

import java.util.Properties;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
/**
 * Update the @configurationproperties field value
 * @author wangbo(wangle_r@163.com)
 */
public interface RefreshConfigurationProperties {
  public void refreshBindingBean(Object bean, ConfigChangeEvent changeEvent);
  public void refreshBinding(String beanName, ConfigChangeEvent changeEvent);
  public void refreshBinding(Object bean, String beanName, ConfigChangeEvent changeEvent);
  public void refreshBinding(Object bean, String beanName, Properties properties);
  public void refreshBinding(Object bean, String beanName);
}
