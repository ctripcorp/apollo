package com.ctrip.framework.apollo.boot;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.env.EnumerableCompositePropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ApolloSpringApplicationRunListener implements SpringApplicationRunListener, PriorityOrdered {
    private static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloPropertySources";

    private static Logger logger = LoggerFactory.getLogger(ApolloSpringApplicationRunListener.class);

    private SpringApplication application;
    private String[] args;

    public ApolloSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    public void starting() {
    }

    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    public void contextPrepared(ConfigurableApplicationContext context) {
    }


    public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();
        String enabled = environment.getProperty("apollo.enabled", "true");
        if (!"true".equals(enabled)) {
            logger.warn("Apollo is not enabled. see property: ${apollo.enabled}");
            return;
        }

        String namespaces = environment.getProperty("apollo.namespaces", "application");
        logger.info("Configured namespaces: {}", namespaces);
        List<String> namespaceList = Arrays.asList(namespaces.split(","));

        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(APOLLO_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }
        CompositePropertySource composite = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);
        for (String namespace : namespaceList) {
            Config config = ConfigService.getConfig(namespace);

            composite.addPropertySource(new ConfigPropertySource(namespace, config));
        }

        propertySources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, composite);

        StringBuilder logInfo = new StringBuilder();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource.getClass().getSimpleName().contains("ConfigurationPropertySources")) {
                //打印文件配置
                try {
                    Field field = propertySource.getClass().getDeclaredField("sources");
                    field.setAccessible(true);
                    List list = (List) ReflectionUtils.getField(field, propertySource);
                    for (Object s : list) {
                        if (s instanceof EnumerableCompositePropertySource) {
                            EnumerableCompositePropertySource enumerableCompositePropertySource = (EnumerableCompositePropertySource) s;

                            Collection<PropertySource<?>> source = enumerableCompositePropertySource.getSource();
                            for (PropertySource<?> a : source) {
                                logInfo.append('\t').append(a.toString()).append("\n");
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    //do nothing
                }
            } else {
                logInfo.append('\t').append(propertySource.toString()).append("\n");
            }

        }
        logger.info("PropertySources piority:\n{}", logInfo.toString());
    }

    public void finished(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {

    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
