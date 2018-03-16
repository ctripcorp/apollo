package com.ctrip.framework.apollo.spring.property;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.ctrip.framework.apollo.spring.annotation.ValueMapping;

/**
 * The class element annotated by {@link ValueMapping}
 * 
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingElement {

  /**
   * Apollo config namespaces
   */
  private final String[] namespaces;

  /**
   * Class element
   */
  private final Member element;

  /**
   * The definition holder array
   */
  private final ValueMappingHolder[] holders;

  public ValueMappingElement(String[] namespaces, Member element, ValueMappingHolder... holders) {
    if (namespaces == null || namespaces.length == 0) {
      throw new IllegalArgumentException("namespaces may not be empty");
    }
    if (element == null) {
      throw new IllegalArgumentException("field may not be null");
    }
    if (holders == null || holders.length == 0) {
      throw new IllegalArgumentException("holders may not be empty");
    }
    this.namespaces = namespaces;
    this.element = element;
    this.holders = holders;
  }

  public String[] getNamespaces() {
    return namespaces;
  }

  public Member getElement() {
    return element;
  }

  public ValueMappingHolder[] getHolders() {
    return holders;
  }

  public ValueMappingHolder getFirstHolder() {
    return holders[0];
  }

  public String getPropKeyDesc() {
    if (holders.length == 1) {
      return holders[0].getPropKey();
    }

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < holders.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(holders[i].getPropKey());
    }
    sb.append("]");
    return sb.toString();
  }

  public boolean isMethod() {
    return element instanceof Method;
  }

  public boolean isField() {
    return element instanceof Field;
  }

  /**
   * Judge if the property key is explicit
   * 
   * @return boolean true: explicit property key, false: ambiguous property key
   */
  public boolean isPropertyKeyExplicit() {
    for (ValueMappingHolder holder : holders) {
      if (holder.hasCollector()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Judge if mapping multiple properties
   * 
   * @return boolean true: mapping multiple properties, false: mapping single property
   */
  public boolean isMappingMultipleProperties() {
    return holders.length > 1 || getFirstHolder().hasCollector();
  }
}
