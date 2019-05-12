package org.springframework.boot.bind.apolloextension;

import org.springframework.boot.bind.ApolloExtensionPropertyNamePatternsMatcher;

/**
 * @author wangbo(wangle_r@163.com)
 */
public class AllPropertyNamePatternsMatcher implements ApolloExtensionPropertyNamePatternsMatcher {

  @Override
  public boolean matches(String propertyName) {
    return true;
  }
}
