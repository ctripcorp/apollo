package com.ctrip.framework.apollo.spring.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Apollo Property Sources processor for Spring Annotation Based Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware {
  private static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";
  private static final Multimap<Integer, String> NAMESPACE_NAMES = HashMultimap.create();
  private static final AtomicBoolean PROPERTY_SOURCES_INITIALIZED = new AtomicBoolean(false);

  private ConfigurableEnvironment environment;

  public static boolean addNamespaces(Collection<String> namespaces, int order) {
    return NAMESPACE_NAMES.putAll(order, namespaces);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (!PROPERTY_SOURCES_INITIALIZED.compareAndSet(false, true)) {
      //already initialized
      return;
    }

    initializePropertySources();
  }

  protected void initializePropertySources() {
    CompositePropertySource composite = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);

    //sort by order asc
    ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
    Iterator<Integer> iterator = orders.iterator();

    while (iterator.hasNext()) {
      int order = iterator.next();
      for (String namespace : NAMESPACE_NAMES.get(order)) {
        Config config = ConfigService.getConfig(namespace);

        composite.addPropertySource(new ConfigPropertySource(namespace, config));
      }
    }
    environment.getPropertySources().addFirst(composite);
  }

  @Override
  public void setEnvironment(Environment environment) {
    //it is safe enough to cast as all known environment is derived from ConfigurableEnvironment
    this.environment = (ConfigurableEnvironment) environment;
  }

  //only for test
   private static void reset() {
    NAMESPACE_NAMES.clear();
    PROPERTY_SOURCES_INITIALIZED.set(false);
  }
}
