package com.ctrip.framework.apollo.spring.property;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;

public class SpringValueRegistry {
  private final Multimap<String, SpringValue> registry = LinkedListMultimap.create();
  // namespace-SpringValue
  private final Multimap<String, SpringValue> namespaceRegistry = LinkedListMultimap.create();
  
  public void register(String key, SpringValue springValue) {
    registry.put(key, springValue);
  }

  public Collection<SpringValue> get(String key) {
    return registry.get(key);
  }  

  public void registerOnNamespace(String key, SpringValue springValue) {
    namespaceRegistry.put(key, springValue);
  }

  public Collection<SpringValue> getByNamespace(String key) {
    return namespaceRegistry.get(key);
  }
}
