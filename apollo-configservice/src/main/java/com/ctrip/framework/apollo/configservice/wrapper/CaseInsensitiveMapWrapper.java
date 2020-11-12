package com.ctrip.framework.apollo.configservice.wrapper;

import java.util.Map;

/**
 * 不区分大小写的Map包装器
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class CaseInsensitiveMapWrapper<T> {

  /**
   * 代表对象
   */
  private final Map<String, T> delegate;

  /**
   * 构造CaseInsensitiveMapWrapper对象
   *
   * @param delegate map对象
   */
  public CaseInsensitiveMapWrapper(Map<String, T> delegate) {
    this.delegate = delegate;
  }

  /**
   * 获取指定key值的value
   *
   * @param key 指定key
   * @return 指定key值的value
   */
  public T get(String key) {
    return delegate.get(key.toLowerCase());
  }

  /**
   * 设置指定key,value
   *
   * @param key   指定key
   * @param value 指定value
   * @return 存放指定key, value
   */
  public T put(String key, T value) {
    return delegate.put(key.toLowerCase(), value);
  }

  /**
   * 移除指定key
   *
   * @param key 指定key
   * @return 移除的key
   */
  public T remove(String key) {
    return delegate.remove(key.toLowerCase());
  }
}
