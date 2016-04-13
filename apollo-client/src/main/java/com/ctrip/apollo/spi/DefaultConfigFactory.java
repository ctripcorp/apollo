package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.apollo.internals.DefaultConfig;
import com.ctrip.apollo.internals.LocalFileConfigRepository;
import com.ctrip.apollo.internals.RemoteConfigRepository;

import org.unidal.lookup.annotation.Named;

import java.io.File;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactory.class, value = "default")
public class DefaultConfigFactory implements ConfigFactory {
  private static final String CONFIG_DIR = "/config-cache";
  private File m_baseDir;

  public DefaultConfigFactory() {
    m_baseDir = new File(ClassLoaderUtil.getClassPath() + CONFIG_DIR);
    if (!m_baseDir.exists()) {
      m_baseDir.mkdir();
    }
  }

  @Override
  public Config create(String namespace) {
    DefaultConfig defaultConfig =
        new DefaultConfig(namespace, createLocalConfigRepository(namespace));
    return defaultConfig;
  }

  LocalFileConfigRepository createLocalConfigRepository(String namespace) {
    LocalFileConfigRepository localFileConfigLoader =
        new LocalFileConfigRepository(m_baseDir, namespace);
    localFileConfigLoader.setFallback(createRemoteConfigRepository(namespace));
    return localFileConfigLoader;
  }

  RemoteConfigRepository createRemoteConfigRepository(String namespace) {
    return new RemoteConfigRepository(namespace);
  }
}
