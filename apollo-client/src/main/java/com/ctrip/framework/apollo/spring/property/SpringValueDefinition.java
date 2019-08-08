package com.ctrip.framework.apollo.spring.property;

public class SpringValueDefinition {

  private final String placeholder;
  private final String propertyName;

  public SpringValueDefinition(String placeholder, String propertyName) {
    this.placeholder = placeholder;
    this.propertyName = propertyName;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public String getPropertyName() {
    return propertyName;
  }
}
