package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangdihang(ivanyangtt@zju.edu.cn)
 */
import static com.ctrip.framework.apollo.spring.config.PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME;

public class DynamicDefaultConfigManager implements EnvironmentAware, BeanFactoryPostProcessor {

  private static ConfigurableEnvironment environment;
  private static AutoUpdateConfigChangeListener autoUpdateConfigChangeListener;
  private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
      .getInstance(ConfigPropertySourceFactory.class);

  /**
   * Listen for new namespaces at runtime.
   */
  public Config addNamespace(String namespace, int order) {
    DefaultConfigFactory df = new DefaultConfigFactory();
    Config config = df.create(namespace);
    PropertySource propertySource = configPropertySourceFactory
        .getConfigPropertySource(namespace, config, order);
    ArrayList<PropertySource<?>> before = new ArrayList<>();
    ArrayList<PropertySource<?>> after = new ArrayList<>();
    CompositePropertySource apollo_composite = ((CompositePropertySource) environment
        .getPropertySources().get(APOLLO_PROPERTY_SOURCE_NAME));
    for (PropertySource ps : apollo_composite.getPropertySources()) {
      if (((ConfigPropertySource) ps).getOrder() < order) {
        before.add(ps);
      } else {
        after.add(ps);
      }
    }
    before.add(propertySource);
    before.addAll(after);
    apollo_composite.getPropertySources().clear();
    apollo_composite.getPropertySources().addAll(before);
    config.addChangeListener(autoUpdateConfigChangeListener);
    autoUpdateConfigChangeListener.updateSpringValueBatch(Config2Map(config));
    return config;
  }

  private Map<String, String> Config2Map(Config config) {
    Map<String, String> map = new HashMap<>();
    for (String key : config.getPropertyNames()) {
      map.put(key, config.getProperty(key, ""));
    }
    return map;
  }

  @Override
  public void setEnvironment(Environment env) {
    environment = (ConfigurableEnvironment) env;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);
  }
}
