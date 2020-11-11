package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置存储库的抽象类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public abstract class AbstractConfigRepository implements ConfigRepository {

  /**
   * RepositoryChangeListener 数组
   */
  private List<RepositoryChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();
  /**
   * Properties工厂对象
   */
  protected PropertiesFactory propertiesFactory = ApolloInjector
      .getInstance(PropertiesFactory.class);

  /**
   * 尝试同步
   *
   * @return true, 同步成功, 否则，false
   */
  protected boolean trySync() {
    try {
      // 同步
      sync();
      // 返回同步成功
      return true;
    } catch (Throwable ex) {
      Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
      log.warn("Sync config failed, will retry. Repository {}, reason: {}", this.getClass(),
          ExceptionUtil.getDetailMessage(ex));
    }
    // 返回同步失败
    return false;
  }

  /**
   * 同步配置
   */
  protected abstract void sync();

  @Override
  public void addChangeListener(RepositoryChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public void removeChangeListener(RepositoryChangeListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * 触发监听器们
   *
   * @param namespace     Namespace 名字
   * @param newProperties 配置
   */
  protected void fireRepositoryChange(String namespace, Properties newProperties) {
    // 循环 RepositoryChangeListener 数组
    for (RepositoryChangeListener listener : m_listeners) {
      try {
        // 触发监听器
        listener.onRepositoryChange(namespace, newProperties);
      } catch (Throwable ex) {
        Tracer.logError(ex);
        log.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
      }
    }
  }
}
