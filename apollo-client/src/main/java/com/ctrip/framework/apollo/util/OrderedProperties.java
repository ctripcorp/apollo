package com.ctrip.framework.apollo.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import lombok.EqualsAndHashCode;

/**
 * OrderedProperties实例将在配置文件中保持外观顺序。
 * <p>
 * 告:
 * <ol>
 *   <li> 需要注意的是，流api或JDk1.8 api(在https://github.com/ctripcorp/apollo/pull/2861中列出)在这里没有实现。</li>
 *  <li>在JDK1.8和以后的jdk之间，{@link Properties}实现是不同的。在JDK10中，{@link Properties}至少有一个单独的实现。因此，这里应该有一个单独的putAll方法。</li>
 *  </ol>
 *
 * @author songdragon@zts.io
 */
@EqualsAndHashCode
public class OrderedProperties extends Properties {

  private static final long serialVersionUID = -1741073539526213291L;
  /**
   * 属性名列表
   */
  private final Set<String> propertyNames;

  /**
   * 构造OrderedProperties，并初始化属性
   */
  public OrderedProperties() {
    propertyNames = Collections.synchronizedSet(new LinkedHashSet<String>());
  }

  @Override
  public synchronized Object put(Object key, Object value) {
    // 添加属性名称
    addPropertyName(key);
    // 添加属性key与value
    return super.put(key, value);
  }

  /**
   * 添加属性名称
   *
   * @param key 属性key
   */
  private void addPropertyName(Object key) {
    if (key instanceof String) {
      propertyNames.add((String) key);
    }
  }

  @Override
  public Set<String> stringPropertyNames() {
    // 返回有序的属性key
    return propertyNames;
  }

  @Override
  public Enumeration<?> propertyNames() {
    // 属性名称集合的枚举
    return Collections.enumeration(propertyNames);
  }

  @Override
  public synchronized Enumeration<Object> keys() {
    // 属性名称集合的枚举实例
    return new Enumeration<Object>() {
      private final Iterator<String> i = propertyNames.iterator();

      @Override
      public boolean hasMoreElements() {
        return i.hasNext();
      }

      @Override
      public Object nextElement() {
        return i.next();
      }
    };
  }

  @Override
  public Set<Object> keySet() {
    // 属性名称key列表
    return new LinkedHashSet<>(propertyNames);
  }


  @Override
  public Set<Entry<Object, Object>> entrySet() {
    Set<Entry<Object, Object>> original = super.entrySet();
    LinkedHashMap<Object, Entry<Object, Object>> entryMap = new LinkedHashMap<>();
    // 设置属性名
    for (String propertyName : propertyNames) {
      entryMap.put(propertyName, null);
    }

    // 设置属性值
    for (Entry<Object, Object> entry : original) {
      entryMap.put(entry.getKey(), entry);
    }

    // 返回value集合
    return new LinkedHashSet<>(entryMap.values());
  }

  @Override
  public synchronized void putAll(Map<?, ?> t) {
    // 保存所有元素
    super.putAll(t);
    for (Object name : t.keySet()) {
      addPropertyName(name);
    }
  }

  @Override
  public synchronized void clear() {
    // 清空
    super.clear();
    this.propertyNames.clear();
  }

  @Override
  public synchronized Object remove(Object key) {
    // 移除
    if (key instanceof String) {
      this.propertyNames.remove(key);
    }
    return super.remove(key);
  }

}
