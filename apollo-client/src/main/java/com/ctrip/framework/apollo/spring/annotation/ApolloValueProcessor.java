package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.spring.auto.SpringValue;
import com.ctrip.framework.foundation.internals.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by zhangzheng on 2018/2/6
 */
public class ApolloValueProcessor extends ApolloProcessor implements EnvironmentAware{

    private Pattern pattern = Pattern.compile("\\$\\{(.*?)(:(.*?))?}");
    private static Multimap<String, SpringValue> monitor = LinkedListMultimap.create();
    private Logger logger = LoggerFactory.getLogger(ApolloValueProcessor.class);


    private Environment environment;

    public static Multimap<String, SpringValue> monitor() {
        return monitor;
    }

    @Override
    protected void processField(Object bean, Field field) {
        ApolloValue apolloValue = AnnotationUtils.getAnnotation(field, ApolloValue.class);
        if(apolloValue==null){
            return;
        }
        Matcher matcher = pattern.matcher(apolloValue.value());
        Preconditions.checkArgument(matcher.matches(),String.format("the apollo value annotation for field:%s is not correct," +
                "please use ${somekey} pattern",field.getType()));
        String key = matcher.group(1);
        String propertyValue = environment.getProperty(key);
        SpringValue springValue = SpringValue.create(bean,field);
        if(!Utils.isBlank(propertyValue)){
            springValue.updateVal(propertyValue);
        }
        DisableAutoUpdate disableAutoUpdate = AnnotationUtils.getAnnotation(field, DisableAutoUpdate.class);
        if(disableAutoUpdate==null){
            monitor.put(key, SpringValue.create(bean, field));
            logger.info("Listening apollo key = {}", key);
        }
        super.processField(bean, field);

    }

    @Override
    protected void processMethod(Object bean, Method method) {

        ApolloValue apolloValue = AnnotationUtils.getAnnotation(method, ApolloValue.class);
        if(apolloValue==null){
            return;
        }
        Matcher matcher = pattern.matcher(apolloValue.value());
        Preconditions.checkArgument(matcher.matches(),String.format("the apollo value annotation for field:%s is not correct," +
                "please use ${somekey} pattern",method.getName()));
        String key = matcher.group(1);
        String propertyValue = environment.getProperty(key);
        SpringValue springValue = SpringValue.create(bean,method);
        if(!Utils.isBlank(propertyValue)){
            springValue.updateVal(propertyValue);
        }
        DisableAutoUpdate disableAutoUpdate = AnnotationUtils.getAnnotation(method, DisableAutoUpdate.class);
        if(disableAutoUpdate==null){
            monitor.put(key, SpringValue.create(bean, method));
            logger.info("Listening apollo key = {}", key);
        }
        super.processMethod(bean,method);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
