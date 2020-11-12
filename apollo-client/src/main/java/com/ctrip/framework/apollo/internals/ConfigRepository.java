package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import java.util.Properties;

/**
 * 配置存储库接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRepository {

  /**
   * 获取配置.
   *
   * @return 从存储库获取的配置信息
   */
  Properties getConfig();

  /**
   * 设置上游的 Repository
   *
   * @param upstreamConfigRepository 上游的Config存储库对象。一般情况下，使用 RemoteConfig存储库对象，读取远程 Config Service
   *                                 的配置
   */
  void setUpstreamRepository(ConfigRepository upstreamConfigRepository);

  /**
   * 添加存储库变化监听器.
   *
   * @param listener 观察变化的监听器
   */
  void addChangeListener(RepositoryChangeListener listener);

  /**
   * 删除观察变化的监听器.
   *
   * @param listener 移除的监听器
   */
  void removeChangeListener(RepositoryChangeListener listener);

  /**
   * 返回配置的源类型，即从哪里加载配置
   *
   * @return 配置源的类型
   */
  ConfigSourceType getSourceType();
}
