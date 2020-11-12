package com.ctrip.framework.foundation.internals;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 服务引导器.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ServiceBootstrap {

  /**
   * 加载指定接口的全部实现类中的第一个实现类.
   *
   * @param clazz 指定接口class
   * @param <S>   指定接口
   * @return 指定接口的全部实现类中的第一个实现类c
   */
  /**
   * 加载指定接口的第一个实现类
   *
   * @param clazz 服务类
   * @param <S>   泛型
   * @return 指定接口的第一个实现类对象
   */
  public static <S> S loadFirst(Class<S> clazz) {
    // 加载指定接口的全部实现类
    Iterator<S> iterator = loadAll(clazz);
    // 文件不存在或不具有正确的实现类,抛出
    if (!iterator.hasNext()) {
      throw new IllegalStateException(String.format(
          "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
          clazz.getName()));
    }
    return iterator.next();
  }

  /**
   * 加载指定Class接口的实现类.
   *
   * @param clazz 指定接口的class
   * @param <S>   泛型
   * @return 实现类Iterator
   */
  public static <S> Iterator<S> loadAll(Class<S> clazz) {
    // 加载一系列有某种共同特征的实现类，即clazz的实现类
    ServiceLoader<S> loader = ServiceLoader.load(clazz);
    return loader.iterator();
  }

  /**
   * 加载指定Class接口的实现类(按优先级排序).
   *
   * @param clazz 指定接口的class
   * @param <S>   指定接口
   * @return 实现类Iterator(按优先级排序)
   */
  public static <S extends Ordered> List<S> loadAllOrdered(Class<S> clazz) {
    // 加载一系列有某种共同特征的实现类，即clazz的实现类
    Iterator<S> iterator = loadAll(clazz);
    // 文件不存在或不具有正确的实现类,抛出
    if (!iterator.hasNext()) {
      throw new IllegalStateException(String.format(
          "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
          clazz.getName()));
    }

    // 实现类列表按优先级排序
    List<S> candidates = Lists.newArrayList(iterator);
    Collections.sort(candidates, new Comparator<S>() {
      @Override
      public int compare(S o1, S o2) {
        // order越小，优先级越高
        return Integer.compare(o1.getOrder(), o2.getOrder());
      }
    });
    return candidates;
  }

  /**
   * 加载指定接口的全部实现类中优先级最高的实现类.
   *
   * @param clazz 指定接口class
   * @param <S>   指定接口
   * @return 指定接口的全部实现类中优先级最高的实现类
   */
  public static <S extends Ordered> S loadPrimary(Class<S> clazz) {
    List<S> candidates = loadAllOrdered(clazz);

    return candidates.get(0);
  }
}
