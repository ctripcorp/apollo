package com.ctrip.apollo.client;

import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.loader.ConfigLoaderFactory;
import com.ctrip.apollo.client.util.ConfigUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Client side config manager
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigManager implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {
    public static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloConfigProperties";
    private static AtomicReference<ApolloConfigManager> singletonProtector = new AtomicReference<ApolloConfigManager>();

    private ConfigLoader configLoader;
    private ConfigurableApplicationContext applicationContext;

    private CompositePropertySource currentPropertySource;

    public ApolloConfigManager() {
        if(!singletonProtector.compareAndSet(null, this)) {
           throw new IllegalStateException("There should be only one ApolloConfigManager instance!");
        }
        this.configLoader = ConfigLoaderFactory.getInstance().getConfigLoader();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new RuntimeException(
                    String.format("ApplicationContext must implement ConfigurableApplicationContext, but found: %s", applicationContext.getClass().getName()));
        }
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        ConfigUtil.getInstance().setApplicationContext(applicationContext);
    }

    /**
     * This is the first method invoked, so we could prepare the property sources here.
     * Specifically we need to finish preparing property source before PropertySourcesPlaceholderConfigurer
     * so that configurations could be injected correctly
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerDependentBeans(registry);
        initializePropertySource();
    }

    /**
     * Register beans needed for Apollo Config Client
     * <li>
     * - RefreshScope: used to refresh beans when configurations changes
     * </li>
     * <li>
     * - PropertySourcesPlaceholderConfigurer: used to support placeholder configuration injection
     * </li>
     * @param registry
     */
    private void registerDependentBeans(BeanDefinitionRegistry registry) {
        BeanDefinition refreshScope = BeanDefinitionBuilder.genericBeanDefinition(RefreshScope.class).getBeanDefinition();
        registry.registerBeanDefinition("refreshScope", refreshScope);
        BeanDefinition propertySourcesPlaceholderConfigurer = BeanDefinitionBuilder.genericBeanDefinition(PropertySourcesPlaceholderConfigurer.class).getBeanDefinition();
        registry.registerBeanDefinition("propertySourcesPlaceholderConfigurer", propertySourcesPlaceholderConfigurer);
    }

    /**
     * This is executed after postProcessBeanDefinitionRegistry
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * Make sure this bean is called before other beans
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Prepare property sources
     * First try to load from remote
     * If loading from remote failed, then fall back to local cached properties
     */
    void initializePropertySource() {
        currentPropertySource = loadPropertySource();

        MutablePropertySources currentPropertySources = applicationContext.getEnvironment().getPropertySources();
        if (currentPropertySources.contains(currentPropertySource.getName())) {
            currentPropertySources.remove(currentPropertySource.getName());
        }
        currentPropertySources.addFirst(currentPropertySource);
    }

    CompositePropertySource loadPropertySource() {
        CompositePropertySource compositePropertySource = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);
        compositePropertySource.addPropertySource(configLoader.loadPropertySource());
        return compositePropertySource;
    }
}
