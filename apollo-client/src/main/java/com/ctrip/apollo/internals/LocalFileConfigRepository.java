package com.ctrip.apollo.internals;

import com.google.common.base.Preconditions;

import com.ctrip.apollo.util.ConfigUtil;
import com.dianping.cat.Cat;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalFileConfigRepository extends AbstractConfigRepository
    implements RepositoryChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(LocalFileConfigRepository.class);
  private final PlexusContainer m_container;
  private final String m_namespace;
  private final File m_baseDir;
  private final ConfigUtil m_configUtil;
  private volatile Properties m_fileProperties;
  private volatile ConfigRepository m_fallback;

  public LocalFileConfigRepository(File baseDir, String namespace) {
    m_baseDir = baseDir;
    m_namespace = namespace;
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load component!", e);
    }
  }

  @Override
  public Properties getConfig() {
    if (m_fileProperties == null) {
      initLocalConfig();
    }
    Properties result = new Properties();
    result.putAll(m_fileProperties);
    return result;
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //clear previous listener
    if (m_fallback != null) {
      m_fallback.removeChangeListener(this);
    }
    m_fallback = fallbackConfigRepository;
    fallbackConfigRepository.addChangeListener(this);
  }

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    if (newProperties.equals(m_fileProperties)) {
      return;
    }
    Properties newFileProperties = new Properties();
    newFileProperties.putAll(newProperties);
    this.m_fileProperties = newFileProperties;
    persistLocalCacheFile(m_baseDir, m_namespace);
    this.fireRepositoryChange(namespace, newProperties);
  }

  void initLocalConfig() {
    try {
      m_fileProperties = this.loadFromLocalCacheFile(m_baseDir, m_namespace);
    } catch (Throwable ex) {
      logger.error("Load config from local config cache file failed", ex);
    }

    //TODO check whether properties is expired or should we return after it's synced with fallback?
    if (m_fileProperties != null) {
      return;
    }

    if (m_fallback == null) {
      throw new RuntimeException(
          "Load config from local config cache failed and there is no fallback repository!");
    }

    try {
      m_fileProperties = m_fallback.getConfig();
      persistLocalCacheFile(m_baseDir, m_namespace);
    } catch (Throwable ex) {
      String message =
          String.format("Load config from fallback repository %s failed", m_fallback.getClass());
      logger.error(message, ex);
      throw new RuntimeException(message, ex);
    }
  }

  private Properties loadFromLocalCacheFile(File baseDir, String namespace) throws IOException {
    Preconditions.checkNotNull(baseDir, "Basedir cannot be null");

    File file = assembleLocalCacheFile(baseDir, namespace);
    Properties properties = null;

    if (file.isFile() && file.canRead()) {
      InputStream in = null;

      try {
        in = new FileInputStream(file);

        properties = new Properties();
        properties.load(in);
      } catch (IOException e) {
        logger.error("Loading config from local cache file {} failed", file.getAbsolutePath(), e);
        Cat.logError(e);
        throw e;
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException e) {
          // ignore
        }
      }
    } else {
      String message =
          String.format("Cannot read from local cache file %s", file.getAbsolutePath());
      logger.error(message);
      throw new RuntimeException(message);
    }

    return properties;
  }

  void persistLocalCacheFile(File baseDir, String namespace) {
    if (baseDir == null) {
      return;
    }
    File file = assembleLocalCacheFile(baseDir, namespace);

    OutputStream out = null;

    try {
      out = new FileOutputStream(file);
      m_fileProperties.store(out, "Persisted by DefaultConfig");
    } catch (IOException ex) {
      logger.error("Persist local cache file {} failed", file.getAbsolutePath(), ex);
      Cat.logError(ex);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  File assembleLocalCacheFile(File baseDir, String namespace) {
    String fileName = String.format("%s-%s-%s.properties", m_configUtil.getAppId(),
        m_configUtil.getCluster(), namespace);
    return new File(baseDir, fileName);
  }
}
