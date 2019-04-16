package com.ctrip.framework.apollo.spring.property;

import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class ScopeBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements EnvironmentAware, BeanFactoryAware {
    private ConfigurableEnvironment environment;
    private BeanFactory beanFactory;
    private PropertySourcesPropertyResolver propertySourcesPropertyResolver;
    private final PlaceholderHelper placeholderHelper;
    private TypeConverter typeConverter;
    private static final Logger logger = LoggerFactory.getLogger(ScopeBeanPostProcessor.class);


    public ScopeBeanPostProcessor() {
        this.placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) this.beanFactory;

    }


    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = null;
        if (beanFactory instanceof BeanDefinitionRegistry) {
            beanName2SpringValueDefinitions = SpringValueDefinitionProcessor
                    .getBeanName2SpringValueDefinitions((BeanDefinitionRegistry) beanFactory);
        }

        if (propertySourcesPropertyResolver == null) {
            propertySourcesPropertyResolver = new PropertySourcesPropertyResolver(environment.getPropertySources());
        }

        if (typeConverter == null) {
            ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
            this.typeConverter = configurableBeanFactory.getTypeConverter();

        }


        Collection<SpringValueDefinition> propertySpringValues = beanName2SpringValueDefinitions
                .get(beanName);
        if (propertySpringValues == null || propertySpringValues.isEmpty()) {
            //不进行处理
            return super.postProcessPropertyValues(pvs, pds, bean, beanName);
        }

        for (PropertyValue propertyValue : pvs.getPropertyValues()) {
            //获取 placeholder
            String placeholder = null;
            for (SpringValueDefinition springValueDefinition : propertySpringValues) {
                if (springValueDefinition.getPropertyName().equals(propertyValue.getName())) {
                    placeholder = springValueDefinition.getPlaceholder();
                    break;
                }
            }
            PropertyDescriptor pd = BeanUtils
                    .getPropertyDescriptor(bean.getClass(), propertyValue.getName());
            Method method = pd.getWriteMethod();
            if (method == null) {
                continue;
            }
            SpringValue springValue = new SpringValue(null, placeholder,
                    bean, beanName, method, false);
            Object resolverValue = resolvePropertyValue(springValue);

            propertyValue.setConvertedValue(resolverValue);

            propertyValue.getConvertedValue().getClass();
        }

        return pvs;
    }

    private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
        try {
            TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
        } catch (Throwable ex) {
            return false;
        }

        return true;
    }

    private Object resolvePropertyValue(SpringValue springValue) {
        // value will never be null, as @Value and @ApolloJsonValue will not allow that
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        Object value = placeholderHelper
                .resolvePropertyValue(configurableBeanFactory, springValue.getBeanName(), springValue.getPlaceholder());


        value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(),
                springValue.getMethodParameter());


        return value;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


}

