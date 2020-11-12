package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.spring.spi.ConfigPropertySourcesProcessorHelper;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * 基于Spring XML应用程序的Apollo属性源处理器
 * <p>
 * 自动注入 ConfigPropertySourcesProcessor bean 对象，当不存在 PropertySourcesProcessor 时，以实现 Apollo 配置的自动加载
 * </p>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySourcesProcessor extends PropertySourcesProcessor
    implements BeanDefinitionRegistryPostProcessor {

  private ConfigPropertySourcesProcessorHelper helper = ServiceBootstrap
      .loadPrimary(ConfigPropertySourcesProcessorHelper.class);

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    // 注入 ConfigPropertySourcesProcessor bean 对象
    helper.postProcessBeanDefinitionRegistry(registry);
  }
}
