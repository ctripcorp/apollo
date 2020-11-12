package com.ctrip.framework.apollo.spring.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Apollo Spring Java Config 注册器Apollo配置注册器帮助类接口
 */
public interface ApolloConfigRegistrarHelper extends Ordered {

  /**
   * 注册定义的BeanDefinitions
   *
   * @param importingClassMetadata 导入的类元数据（对特定类注解的抽象访问，其形式不需要加载该类）
   * @param registry               保存bean定义的注册表
   */
  void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry);
}
