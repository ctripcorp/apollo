package com.ctrip.framework.apollo.internals;

import java.util.Properties;

/**
 * 存储库更改监听接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RepositoryChangeListener {

  /**
   * 当配置存储库更改时调用.
   *
   * @param namespace     此存储库更改的名称空间
   * @param newProperties 更改后的属性
   */
  void onRepositoryChange(String namespace, Properties newProperties);
}
