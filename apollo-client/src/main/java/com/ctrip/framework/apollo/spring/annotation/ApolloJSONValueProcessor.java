package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.foundation.internals.Utils;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

/**
 * Create by zhangzheng on 2018/2/6
 */
public class ApolloJSONValueProcessor extends ApolloProcessor implements EnvironmentAware {

  private Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
  private Logger logger = LoggerFactory.getLogger(ApolloJSONValueProcessor.class);

  private static Gson gson = new Gson();


  private Environment environment;


  @Override
  protected void processField(Object bean, Field field) {
    ApolloJSONValue apolloJSONValue = AnnotationUtils.getAnnotation(field, ApolloJSONValue.class);
    if (apolloJSONValue == null) {
      return;
    }
    Matcher matcher = pattern.matcher(apolloJSONValue.value());
    Preconditions.checkArgument(matcher.matches(),
        String.format("the apollo value annotation for field:%s is not correct," +
            "please use ${somekey} pattern", field.getType()));
    String key = matcher.group(1);
    String propertyValue = environment.getProperty(key);
    if (!Utils.isBlank(propertyValue)) {
      try {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(bean, gson.fromJson(propertyValue, field.getGenericType()));
        field.setAccessible(accessible);
      } catch (Exception e) {
        logger.error("set json value exception", e);
      }
    }

    super.processField(bean, field);

  }

  @Override
  protected void processMethod(Object bean, Method method) {

    ApolloJSONValue apolloJSONValue = AnnotationUtils.getAnnotation(method, ApolloJSONValue.class);
    if (apolloJSONValue == null) {
      return;
    }
    Matcher matcher = pattern.matcher(apolloJSONValue.value());
    Preconditions.checkArgument(matcher.matches(),
        String.format("the apollo value annotation for field:%s is not correct," +
            "please use ${somekey} pattern", method.getName()));
    String key = matcher.group(1);
    String propertyValue = environment.getProperty(key);
    if (!Utils.isBlank(propertyValue)) {
      try {
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Type[] types = method.getGenericParameterTypes();
        Preconditions.checkArgument(types.length == 1, String.format("the "));
        method.invoke(bean, gson.fromJson(propertyValue, types[0]));
        method.setAccessible(accessible);
      } catch (Exception e) {
        logger.error("set json value exception", e);
      }
    }
    super.processMethod(bean, method);
  }


  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

}
