package com.ctrip.apollo.internals;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import com.ctrip.apollo.util.ConfigUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unidal.lookup.ComponentTestCase;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Jason on 4/9/16.
 */
public class LocalFileConfigRepositoryTest extends ComponentTestCase {
  private File someBaseDir;
  private String someNamespace;
  private ConfigRepository fallbackRepo;
  private Properties someProperties;
  private static String someAppId = "someApp";
  private static String someCluster = "someCluster";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    someBaseDir = new File("src/test/resources/config-cache");
    someBaseDir.mkdir();

    someNamespace = "someName";
    someProperties = new Properties();
    someProperties.setProperty("defaultKey", "defaultValue");
    fallbackRepo = mock(ConfigRepository.class);
    when(fallbackRepo.getConfig()).thenReturn(someProperties);

    defineComponent(ConfigUtil.class, MockConfigUtil.class);
  }

  @After
  public void tearDown() throws Exception {
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
    return String.format("%s-%s-%s.properties", someAppId,
            someCluster, someNamespace);
  }

  @Test
  public void testLoadConfig() throws Exception {

  }

  @Test
  public void testLoadConfigWithLocalFile() throws Exception {
    File file = new File(someBaseDir, assembleLocalCacheFileName());

    String someKey = "someKey";
    String someValue = "someValue";

    Files.write(someKey + "=" + someValue, file, Charsets.UTF_8);

    LocalFileConfigRepository localRepo = new LocalFileConfigRepository(someBaseDir, someNamespace);
    Properties properties = localRepo.getConfig();

    assertEquals(someValue, properties.getProperty(someKey));

  }

  @Test
  public void testLoadConfigWithNoLocalFile() throws Exception {
    LocalFileConfigRepository
        localFileConfigRepository =
        new LocalFileConfigRepository(someBaseDir, someNamespace);

    localFileConfigRepository.setFallback(fallbackRepo);

    Properties result = localFileConfigRepository.getConfig();

    assertThat(
        "LocalFileConfigRepository's properties should be the same as fallback repo's when there is no local cache",
        result.entrySet(), equalTo(someProperties.entrySet()));
  }

  @Test
  public void testLoadConfigWithNoLocalFileMultipleTimes() throws Exception {
    LocalFileConfigRepository localRepo =
        new LocalFileConfigRepository(someBaseDir, someNamespace);

    localRepo.setFallback(fallbackRepo);

    Properties someProperties = localRepo.getConfig();

    LocalFileConfigRepository
        anotherLocalRepoWithNoFallback =
        new LocalFileConfigRepository(someBaseDir, someNamespace);

    Properties anotherProperties = anotherLocalRepoWithNoFallback.getConfig();

    assertThat(
        "LocalFileConfigRepository should persist local cache files and return that afterwards",
            someProperties.entrySet(), equalTo(anotherProperties.entrySet()));

  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    RepositoryChangeListener someListener = mock(RepositoryChangeListener.class);

    LocalFileConfigRepository localFileConfigRepository =
        new LocalFileConfigRepository(someBaseDir, someNamespace);
    localFileConfigRepository.setFallback(fallbackRepo);
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

}
