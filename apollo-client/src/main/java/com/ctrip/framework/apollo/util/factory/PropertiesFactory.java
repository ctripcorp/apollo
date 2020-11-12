package com.ctrip.framework.apollo.util.factory;

import java.util.Properties;

/**
 * 构造属性实例的工厂接口.
 *
 * @author songdragon@zts.io
 */
public interface PropertiesFactory {

  /**
   * 内存中的配置项是否保持和页面上的顺序一致配置
   * <p>默认情况下，apollo client内存中的配置存放在Properties中（底下是Hashtable），不会刻意保持和页面上看到的顺序一致，对绝大部分的场景是没有影响的。</p>
   * <p>不过有些场景会强依赖配置项的顺序（如spring cloud zuul的路由规则），针对这种情况，可以开启OrderedProperties特性来使得内存中的配置顺序和页面上看到的一致。</p>
   */
  String APOLLO_PROPERTY_ORDER_ENABLE = "apollo.property.order.enable";

  /**
   * 获取Properties实例
   * <pre>
   * 默认的实现:
   * 1. 如果APOLLO_PROPERTY_ORDER_ENABLE为真，则返回一个{@link com.ctrip.framework.apollo.util.OrderedProperties}的实例。
   * 2. 否则返回{@link Properties}的新实例
   *
   * @return properties对象
   */
  Properties getPropertiesInstance();
}
