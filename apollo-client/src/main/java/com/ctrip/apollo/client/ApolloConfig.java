package com.ctrip.apollo.client;

import com.ctrip.apollo.client.impl.MockConfigLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * Client side config
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfig implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {
    public static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloConfigProperties";

    private ConfigLoader configLoader;

    private ConfigurableApplicationContext applicationContext;

    public ApolloConfig() {
        this.configLoader = new MockConfigLoader();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new RuntimeException(
                    String.format("ApplicationContext must implement ConfigurableApplicationContext, but found: %s", applicationContext.getClass().getName()));
        }
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * This is the first method invoked, so we could prepare the property sources here
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        preparePropertySource();
    }

    /**
     * This is executed after postProcessBeanDefinitionRegistry
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    void preparePropertySource() {
        MutablePropertySources currentPropertySources = applicationContext.getEnvironment().getPropertySources();
        if (currentPropertySources.contains(APOLLO_PROPERTY_SOURCE_NAME)) {
            currentPropertySources.remove(APOLLO_PROPERTY_SOURCE_NAME);
        }

        CompositePropertySource composite = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);
        composite.addPropertySource(configLoader.loadPropertySource());
        applicationContext.getEnvironment().getPropertySources().addFirst(composite);
    }
}
