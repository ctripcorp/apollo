package org.springframework.boot.bind;

import java.util.Collection;

import org.springframework.core.env.PropertySources;

/**
 * @author wangbo(wangle_r@163.com)
 */
public class ApolloExtensionPropertySourcesPropertyValues extends PropertySourcesPropertyValues {
  public ApolloExtensionPropertySourcesPropertyValues(PropertySources propertySources,
                                                      Collection<String> nonEnumerableFallbackNames,
                                                      PropertyNamePatternsMatcher includes,
                                                      boolean resolvePlaceholders) {
    super(propertySources, nonEnumerableFallbackNames, includes, resolvePlaceholders);
  }

}
