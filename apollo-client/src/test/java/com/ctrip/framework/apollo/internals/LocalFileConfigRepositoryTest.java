package com.ctrip.framework.apollo.internals;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.util.ConfigUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unidal.lookup.ComponentTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Jason on 4/9/16.
 */
public class LocalFileConfigRepositoryTest extends ComponentTestCase {
  private File someBaseDir;
  private String someNamespace;
  private ConfigRepository upstreamRepo;
  private Properties someProperties;
  private static String someAppId = "someApp";
  private static String someCluster = "someCluster";
  private String defaultKey;
  private String defaultValue;

  @Before
  public void setUp() throws Exception {
    super.tearDown();//clear the container
    super.setUp();
    someBaseDir = new File("src/test/resources/config-cache");
    someBaseDir.mkdir();

    someNamespace = "someName";
    someProperties = new Properties();
    defaultKey = "defaultKey";
    defaultValue = "defaultValue";
    someProperties.setProperty(defaultKey, defaultValue);
    upstreamRepo = mock(ConfigRepository.class);
    when(upstreamRepo.getConfig()).thenReturn(someProperties);

    defineComponent(ConfigUtil.class, MockConfigUtil.class);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    recursiveDelete(someBaseDir);
  }

  //helper method to clean created files
  private void recursiveDelete(File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        recursiveDelete(f);
      }
    }
    file.delete();
  }

  private String assembleLocalCacheFileName() {
    return String.format("%s.properties", Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(someAppId, someCluster, someNamespace));
  }


  @Test
  public void testLoadConfigWithLocalFile() throws Exception {

    String someKey = "someKey";
    String someValue = "someValue\nxxx\nyyy";

    Properties someProperties = new Properties();
    someProperties.setProperty(someKey, someValue);
    createLocalCachePropertyFile(someProperties);

    LocalFileConfigRepository localRepo = new LocalFileConfigRepository(someNamespace);
    localRepo.setLocalCacheDir(someBaseDir, true);
    Properties properties = localRepo.getConfig();

    assertEquals(someValue, properties.getProperty(someKey));

  }

  @Test
  public void testLoadConfigWithLocalFileAndFallbackRepo() throws Exception {
    File file = new File(someBaseDir, assembleLocalCacheFileName());

    String someValue = "someValue";

    Files.write(defaultKey + "=" + someValue, file, Charsets.UTF_8);

    LocalFileConfigRepository localRepo = new LocalFileConfigRepository(someNamespace, upstreamRepo);
    localRepo.setLocalCacheDir(someBaseDir, true);

    Properties properties = localRepo.getConfig();

    assertEquals(defaultValue, properties.getProperty(defaultKey));
  }

  @Test
  public void testLoadConfigWithNoLocalFile() throws Exception {
    LocalFileConfigRepository
        localFileConfigRepository =
        new LocalFileConfigRepository(someNamespace, upstreamRepo);
    localFileConfigRepository.setLocalCacheDir(someBaseDir, true);

    Properties result = localFileConfigRepository.getConfig();

    assertThat(
        "LocalFileConfigRepository's properties should be the same as fallback repo's when there is no local cache",
        result.entrySet(), equalTo(someProperties.entrySet()));
  }

  @Test
  public void testLoadConfigWithNoLocalFileMultipleTimes() throws Exception {
    LocalFileConfigRepository localRepo =
        new LocalFileConfigRepository(someNamespace, upstreamRepo);
    localRepo.setLocalCacheDir(someBaseDir, true);

    Properties someProperties = localRepo.getConfig();

    LocalFileConfigRepository
        anotherLocalRepoWithNoFallback =
        new LocalFileConfigRepository(someNamespace);
    anotherLocalRepoWithNoFallback.setLocalCacheDir(someBaseDir, true);

    Properties anotherProperties = anotherLocalRepoWithNoFallback.getConfig();

    assertThat(
        "LocalFileConfigRepository should persist local cache files and return that afterwards",
        someProperties.entrySet(), equalTo(anotherProperties.entrySet()));

  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);

    LocalFileConfigRepository localFileConfigRepository =
        new LocalFileConfigRepository(someNamespace, upstreamRepo);
    localFileConfigRepository.setLocalCacheDir(someBaseDir, true);
    localFileConfigRepository.addChangeListener(someListener);

    localFileConfigRepository.getConfig();

    Properties anotherProperties = new Properties();
    anotherProperties.put("anotherKey", "anotherValue");

    localFileConfigRepository.onRepositoryChange(someNamespace, anotherProperties);

    final ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);

    verify(someListener, times(1)).onRepositoryChange(eq(someNamespace), captor.capture());

    assertEquals(anotherProperties, captor.getValue());

  }

  public static class MockConfigUtil extends ConfigUtil {
    @Override
    public String getAppId() {
      return someAppId;
    }

    @Override
    public String getCluster() {
      return someCluster;
    }
  }

  private File createLocalCachePropertyFile(Properties properties) throws IOException {
    File file = new File(someBaseDir, assembleLocalCacheFileName());
    FileOutputStream in = null;
    try {
      in = new FileOutputStream(file);
      properties.store(in, "Persisted by LocalFileConfigRepositoryTest");
    } finally {
      if (in != null) {
        in.close();
      }
    }
    return file;
  }
}
