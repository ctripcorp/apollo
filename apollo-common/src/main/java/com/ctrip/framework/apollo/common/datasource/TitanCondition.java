package com.ctrip.framework.apollo.common.datasource;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class TitanCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    if (!StringUtils.isEmpty(context.getEnvironment().getProperty("fat.titan.url"))) {
      return true;
    } else if (!StringUtils.isEmpty(context.getEnvironment().getProperty("uat.titan.url"))) {
      return true;
    } else if (!StringUtils.isEmpty(context.getEnvironment().getProperty("pro.titan.url"))) {
      return true;
    }
    return false;
  }

}
