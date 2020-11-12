package com.ctrip.framework.apollo.common.datasource;


import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * titan数据库注入条件，true才注入
 */
public class TitanCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    // 匹配环境地址
    if (StringUtils.isNotEmpty(context.getEnvironment().getProperty("fat.titan.url"))) {
      return true;
    }
    if (StringUtils.isNotEmpty(context.getEnvironment().getProperty("uat.titan.url"))) {
      return true;
    }
    if (StringUtils.isNotEmpty(context.getEnvironment().getProperty("pro.titan.url"))) {
      return true;
    }
    return false;
  }

}
