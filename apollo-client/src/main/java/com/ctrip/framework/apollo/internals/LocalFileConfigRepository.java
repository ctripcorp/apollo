package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件配置 Repository实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class LocalFileConfigRepository extends AbstractConfigRepository
    implements RepositoryChangeListener {

  /**
   * 配置文件目录
   */
  private static final String CONFIG_DIR = "/c onfig-cache";
  /**
   * 名称空间名称
   */
  private final String m_namespace;
  /**
   * 本地缓存配置文件目录
   */
  private File m_baseDir;
  /**
   * 配置工具类
   */
  private final ConfigUtil m_configUtil;
  /**
   * 配置文件 Properties
   */
  private volatile Properties m_fileProperties;
  /**
   * 上游的 ConfigRepository 对象。一般情况下，使用 RemoteConfigRepository 对象，读取远程 Config Service 的配置
   */
  private volatile ConfigRepository m_upstream;

  /**
   * 数据源类型，默认为本地
   */
  private volatile ConfigSourceType m_sourceType = ConfigSourceType.LOCAL;

  /**
   * 构建LocalFileConfigRepository.
   *
   * @param namespace 指定名称空间名称
   */
  public LocalFileConfigRepository(String namespace) {
    this(namespace, null);
  }

  /**
   * * 构建LocalFileConfigRepository.
   *
   * @param namespace 指定名称空间名称
   * @param upstream  指定上游的 ConfigRepository 对象
   */
  public LocalFileConfigRepository(String namespace, ConfigRepository upstream) {
    m_namespace = namespace;
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    this.setLocalCacheDir(findLocalCacheDir(), false);
    this.setUpstreamRepository(upstream);
    this.trySync();
  }

  /**
   * 设置本地缓存配置文件目录
   *
   * @param baseDir         本地缓存配置文件目录
   * @param syncImmediately 是否立即同步
   */
  void setLocalCacheDir(File baseDir, boolean syncImmediately) {
    m_baseDir = baseDir;
    // 获得本地缓存配置文件的目录
    this.checkLocalConfigCacheDir(m_baseDir);
    // 若需要立即同步，则进行同步
    if (syncImmediately) {
      this.trySync();
    }
  }

  /**
   * 获得本地缓存目录
   *
   * @return 本地缓存目录
   */
  private File findLocalCacheDir() {
    try {
      // 获得默认缓存配置目录
      String defaultCacheDir = m_configUtil.getDefaultLocalCacheDir();
      // 若不存在该目录，进行创建
      Path path = Paths.get(defaultCacheDir);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
      // 返回该目录下的 CONFIG_DIR 目录
      if (Files.exists(path) && Files.isWritable(path)) {
        return new File(defaultCacheDir, CONFIG_DIR);
      }
    } catch (Throwable ex) {
      //ignore
    }
    // 若失败，使用 ClassPath 下的 CONFIG_DIR 目录
    return new File(ClassLoaderUtil.getClassPath(), CONFIG_DIR);
  }

  @Override
  public Properties getConfig() {
    // 如果 `m_fileProperties` 为空，强制同步
    if (m_fileProperties == null) {
      sync();
    }
    // 返回新创建的 `m_fileProperties` 对象，避免原有对象被修改。
    Properties result = propertiesFactory.getPropertiesInstance();
    result.putAll(m_fileProperties);
    return result;
  }

  @Override
  public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
    if (upstreamConfigRepository == null) {
      return;
    }
    // 从旧的 `m_upstream` 移除自己
    if (m_upstream != null) {
      m_upstream.removeChangeListener(this);
    }
    // 设置新的 `m_upstream`
    m_upstream = upstreamConfigRepository;
    // 从 `m_upstream` 拉取配置
    trySyncFromUpstream();
    // 向新的 `m_upstream` 注册自己
    upstreamConfigRepository.addChangeListener(this);
  }

  @Override
  public ConfigSourceType getSourceType() {
    return m_sourceType;
  }

  @Override
  public void onRepositoryChange(String namespace, Properties newProperties) {
    // 忽略，若未变更
    if (newProperties.equals(m_fileProperties)) {
      return;
    }
    // 读取新的 Properties 对象
    Properties newFileProperties = propertiesFactory.getPropertiesInstance();
    newFileProperties.putAll(newProperties);
    // 更新到 `m_fileProperties` 中
    updateFileProperties(newFileProperties, m_upstream.getSourceType());
    // 发布 Repository 的配置发生变化，触发对应的监听器们
    this.fireRepositoryChange(namespace, newProperties);
  }

  @Override
  protected void sync() {
    //sync with upstream immediately
    // 从 `m_upstream` 同步配置
    boolean syncFromUpstreamResultSuccess = trySyncFromUpstream();
    // 若成功，则直接返回
    if (syncFromUpstreamResultSuccess) {
      return;
    }

    // 若失败，读取本地缓存的配置文件
    Transaction transaction = Tracer.newTransaction("Apollo.ConfigService", "syncLocalConfig");
    Throwable exception = null;
    try {
      transaction.addData("Basedir", m_baseDir.getAbsolutePath());
      // 加载本地缓存的配置文件
      m_fileProperties = this.loadFromLocalCacheFile(m_baseDir, m_namespace);
      m_sourceType = ConfigSourceType.LOCAL;
      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
      transaction.setStatus(ex);
      exception = ex;
      //ignore
    } finally {
      transaction.complete();
    }

    // 若未读取到缓存的配置文件，抛出异常
    if (m_fileProperties == null) {
      m_sourceType = ConfigSourceType.NONE;
      throw new ApolloConfigException(
          "Load config from local config failed!", exception);
    }
  }

  /**
   * 从 m_upstream 拉取初始配置，并返回是否拉取成功
   *
   * @return true, 成功，否则，false
   */
  private boolean trySyncFromUpstream() {
    if (m_upstream == null) {
      return false;
    }
    try {
      // 更新到 `m_fileProperties` 中
      updateFileProperties(m_upstream.getConfig(), m_upstream.getSourceType());
      return true;
    } catch (Throwable ex) {
      Tracer.logError(ex);
      log
          .warn("Sync config from upstream repository {} failed, reason: {}", m_upstream.getClass(),
              ExceptionUtil.getDetailMessage(ex));
    }
    return false;
  }

  /**
   * 更新文件Properties文件，若Properties发生变化，向缓存配置文件写入 Properties
   *
   * @param newProperties 新的Properties对象
   * @param sourceType    配置源类型
   */
  private synchronized void updateFileProperties(Properties newProperties,
      ConfigSourceType sourceType) {
    this.m_sourceType = sourceType;
    // 忽略，若未变更
    if (newProperties.equals(m_fileProperties)) {
      return;
    }
    // 设置新的 Properties 到 `m_fileProperties` 中。
    this.m_fileProperties = newProperties;
    // 持久化到本地缓存配置文件
    persistLocalCacheFile(m_baseDir, m_namespace);
  }

  /**
   * 从缓存配置文件，读取 Properties
   *
   * @param baseDir   本地缓存配置文件目录
   * @param namespace 名称空间名称
   * @return 读取后的Properties对象
   */
  private Properties loadFromLocalCacheFile(File baseDir, String namespace) {
    Preconditions.checkNotNull(baseDir, "Basedir cannot be null");

    // 拼接本地缓存的配置文件 File 对象
    File file = assembleLocalCacheFile(baseDir, namespace);
    // 从文件中，读取 Properties
    Properties properties = null;

    if (file.isFile() && file.canRead()) {
      try (InputStream in = new FileInputStream(file)) {

        properties = propertiesFactory.getPropertiesInstance();
        // 读取
        properties.load(in);
        log.debug("Loading local config file {} successfully!", file.getAbsolutePath());
      } catch (IOException ex) {
        Tracer.logError(ex);
        throw new ApolloConfigException(String
            .format("Loading config from local cache file %s failed", file.getAbsolutePath()), ex);
      }
    } else {
      throw new ApolloConfigException(
          String.format("Cannot read from local cache file %s", file.getAbsolutePath()));
    }

    return properties;
  }

  /**
   * 向缓存配置文件，写入 Properties
   *
   * @param baseDir   本地缓存配置文件目录
   * @param namespace 名称空间名称
   */
  void persistLocalCacheFile(File baseDir, String namespace) {
    if (baseDir == null) {
      return;
    }
    // 拼接本地缓存的配置文件 File 对象
    File file = assembleLocalCacheFile(baseDir, namespace);

    Transaction transaction = Tracer
        .newTransaction("Apollo.ConfigService", "persistLocalConfigFile");
    transaction.addData("LocalConfigFile", file.getAbsolutePath());
    // 向文件中，写入 Properties
    try (OutputStream out = new FileOutputStream(file)) {

      m_fileProperties.store(out, "Persisted by DefaultConfig");
      transaction.setStatus(Transaction.SUCCESS);
    } catch (IOException ex) {
      ApolloConfigException exception =
          new ApolloConfigException(
              String.format("Persist local cache file %s failed", file.getAbsolutePath()), ex);
      Tracer.logError(exception);
      transaction.setStatus(exception);
      log.warn("Persist local cache file {} failed, reason: {}.", file.getAbsolutePath(),
          ExceptionUtil.getDetailMessage(ex));
    } finally {
      transaction.complete();
    }
  }

  /**
   * 校验本地缓存配置目录是否存在。若不存在，则进行创建
   *
   * @param baseDir 本地缓存配置目录对象
   */
  private void checkLocalConfigCacheDir(File baseDir) {
    // 若本地缓存配置文件的目录已经存在，则返回
    if (baseDir.exists()) {
      return;
    }
    Transaction transaction = Tracer.newTransaction("Apollo.ConfigService", "createLocalConfigDir");
    transaction.addData("BaseDir", baseDir.getAbsolutePath());
    try {
      // 创建本地缓存配置目录
      Files.createDirectory(baseDir.toPath());
      transaction.setStatus(Transaction.SUCCESS);
    } catch (IOException ex) {
      ApolloConfigException exception = new ApolloConfigException(
          String.format("Create local config directory %s failed", baseDir.getAbsolutePath()), ex);
      Tracer.logError(exception);
      transaction.setStatus(exception);
      log.warn(
          "Unable to create local config cache directory {}, reason: {}. Will not able to cache config file.",
          baseDir.getAbsolutePath(), ExceptionUtil.getDetailMessage(ex));
    } finally {
      transaction.complete();
    }
  }

  /**
   * 拼接完整的本地缓存配置文件的地址
   *
   * @param baseDir   本地缓存配置目录对象
   * @param namespace 名称空间名称
   * @return 完整的本地缓存配置文件的地址
   */
  File assembleLocalCacheFile(File baseDir, String namespace) {
    // ${baseDir}/config-cache/ + ${appId}+${cluster} + ${namespace}.properties
    String fileName = String.format("%s.properties",
        Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).join(m_configUtil.getAppId(),
            m_configUtil.getCluster(), namespace));
    return new File(baseDir, fileName);
  }
}
