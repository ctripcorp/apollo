package com.ctrip.framework.apollo.spring.property;

public class SpringValueDefinition {

  private final String key;
  private final String placeholder;
  private final String propertyName;

  public SpringValueDefinition(String key, String placeholder, String propertyName) {
    this.key = key;
    this.placeholder = placeholder;
    this.propertyName = propertyName;
  }

  public String getKey() {
    return key;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public String getPropertyName() {
    return propertyName;
  }
}
