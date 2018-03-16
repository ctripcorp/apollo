package com.ctrip.framework.apollo.spring.config;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingProcessor;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.property.ValueMappingElement;
import com.ctrip.framework.apollo.spring.property.ValueMappingHolder;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;

/**
 * Apollo Property Sources processor for Spring Annotation Based Application. <br /> <br />
 *
 * The reason why PropertySourcesProcessor implements {@link BeanFactoryPostProcessor} instead of
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor} is that lower versions of
 * Spring (e.g. 3.1.1) doesn't support registering BeanDefinitionRegistryPostProcessor in ImportBeanDefinitionRegistrar
 * - {@link com.ctrip.framework.apollo.spring.annotation.ApolloConfigRegistrar}
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware,
    PriorityOrdered, InstantiationAwareBeanPostProcessor {
  private static final Multimap<Integer, String> NAMESPACE_NAMES = LinkedHashMultimap.create();
  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
  private static final AtomicBoolean AUTO_UPDATE_INITIALIZED = new AtomicBoolean(false);

  private final ConfigPropertySourceFactory configPropertySourceFactory = ApolloInjector
      .getInstance(ConfigPropertySourceFactory.class);
  private final ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  private final ValueMappingProcessor valueMappingProcessor = ApolloInjector.getInstance(ValueMappingProcessor.class);
  private final SpringValueRegistry springValueRegistry = ApolloInjector.getInstance(SpringValueRegistry.class);
  private ConfigurableEnvironment environment;
  private AutoUpdateConfigChangeListener autoUpdateConfigChangeListener;
  
  public static boolean addNamespaces(Collection<String> namespaces, int order) {
    return NAMESPACE_NAMES.putAll(order, namespaces);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (INITIALIZED.compareAndSet(false, true)) {
      initializePropertySources();

      autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);
      if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
        initializeAutoUpdatePropertiesFeature();
      }
    }
  }

  private void initializePropertySources() {
    if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME)) {
      //already initialized
      return;
    }
    CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME);

    //sort by order asc
    ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
    Iterator<Integer> iterator = orders.iterator();

    while (iterator.hasNext()) {
      int order = iterator.next();
      for (String namespace : NAMESPACE_NAMES.get(order)) {
        Config config = ConfigService.getConfig(namespace);

        composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
      }
    }

    // add after the bootstrap property source or to the first
    if (environment.getPropertySources()
        .contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
      environment.getPropertySources()
          .addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, composite);
    } else {
      environment.getPropertySources().addFirst(composite);
    }
  }

  private void initializeAutoUpdatePropertiesFeature() {
    if (!AUTO_UPDATE_INITIALIZED.compareAndSet(false, true)) {
      return;
    }
    
    List<ConfigPropertySource> configPropertySources =
        configPropertySourceFactory.getAllConfigPropertySources();
    for (ConfigPropertySource configPropertySource : configPropertySources) {
      configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
    }
  }

  @Override
  public void setEnvironment(Environment environment) {
    //it is safe enough to cast as all known environment is derived from ConfigurableEnvironment
    this.environment = (ConfigurableEnvironment) environment;
  }
  
  @Override
  public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName)
      throws BeansException {
    return null;
  }

  @Override
  public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
    return true;
  }

  @Override
  public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds,
      Object bean, String beanName) throws BeansException {
    // handle the class element with valueMapping annotation
    List<ValueMappingElement> elemList = valueMappingProcessor.createValueMappingElements(bean);
    if (!CollectionUtils.isEmpty(elemList)) {
      for (ValueMappingElement elem : elemList) {
        SpringValue springValue = new SpringValue(bean, beanName, elem);
        // set initial value
        valueMappingProcessor.updateProperty(springValue.getBean(),
            springValue.getValueMappingElement(), environment);

        // register monitor
        if (elem.isPropertyKeyExplicit()) {
          // property key is explicit, monitor on property key
          for (ValueMappingHolder holder : elem.getHolders()) {
            springValueRegistry.register(holder.getPropKey(), springValue);
          }
        } else {
          // property key is ambiguous, monitor on config namespace
          for (String namespace : elem.getNamespaces()) {
            springValueRegistry.registerOnNamespace(namespace, springValue);
          }
        }
      }

      // Since ValueMapping element could be rather complex, it must be able to update automatically, ignore this switch
      if (!configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
        initializeAutoUpdatePropertiesFeature();
      }
    }
    
    return pvs;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }
  
  //only for test
  private static void reset() {
    NAMESPACE_NAMES.clear();
    INITIALIZED.set(false);
    AUTO_UPDATE_INITIALIZED.set(false);
  }

  @Override
  public int getOrder() {
    //make it as early as possible
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
