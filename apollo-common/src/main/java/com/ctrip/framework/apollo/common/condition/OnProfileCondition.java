package com.ctrip.framework.apollo.common.condition;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * 与｛@link spring.profiles.active}配置文件匹配激活Bean的自定义条件
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class OnProfileCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    // 获取环境变量中所有的active值, spring.profile.active=xxx,xxx
    Set<String> activeProfiles = Sets.newHashSet(context.getEnvironment().getActiveProfiles());
    // 获取ConditionalOnProfile中value的所有值
    Set<String> requiredActiveProfiles = retrieveAnnotatedProfiles(metadata,
        ConditionalOnProfile.class.getName());
    // 获取ConditionalOnMissingProfile中value的所有值
    Set<String> requiredInactiveProfiles = retrieveAnnotatedProfiles(metadata,
        ConditionalOnMissingProfile.class
            .getName());

    // 如果从requiredActiveProfiles中过滤activeProfiles存在的值为空并且requiredActiveProfiles和activeProfiles没有交集
    return Sets.difference(requiredActiveProfiles, activeProfiles).isEmpty()
        && Sets.intersection(requiredInactiveProfiles, activeProfiles).isEmpty();
  }

  /**
   * 检索指定注解的所有value值
   *
   * @param metadata       注解类型元数据（对注解元素的封装适配）
   * @param annotationType 注解类型
   * @return 指定注解的所有value值
   */
  private Set<String> retrieveAnnotatedProfiles(AnnotatedTypeMetadata metadata,
      String annotationType) {
    // 根据"全限定注解类名"判断是否标注有该注解
    if (!metadata.isAnnotated(annotationType)) {
      return Collections.emptySet();
    }

    // 指定注解的所有属性
    MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(annotationType);

    if (attributes == null) {
      return Collections.emptySet();
    }

    // 获取value,即
    Set<String> profiles = Sets.newHashSet();
    List<?> values = attributes.get("value");

    if (values != null) {
      for (Object value : values) {
        if (value instanceof String[]) {
          Collections.addAll(profiles, (String[]) value);
        } else {
          profiles.add((String) value);
        }
      }
    }

    return profiles;
  }
}
