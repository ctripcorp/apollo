package com.ctrip.apollo.internals;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import com.ctrip.apollo.ConfigChangeListener;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.apollo.enums.PropertyChangeType;
import com.ctrip.apollo.model.ConfigChange;
import com.ctrip.apollo.model.ConfigChangeEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfigTest {
  private File someResourceDir;
  private String someNamespace;
  private ConfigRepository configRepository;
  private Properties someProperties;

  @Before
  public void setUp() throws Exception {
    someResourceDir = new File(ClassLoaderUtil.getClassPath() + "/META-INF/config");
    someResourceDir.mkdirs();
    someNamespace = "someName";
    configRepository = mock(ConfigRepository.class);
  }

  @After
  public void tearDown() throws Exception {
    recursiveDelete(someResourceDir);
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

  @Test
  public void testGetPropertyWithAllPropertyHierarchy() throws Exception {
    String someKey = "someKey";
    String someSystemPropertyValue = "system-property-value";

    String anotherKey = "anotherKey";
    String someLocalFileValue = "local-file-value";

    String lastKey = "lastKey";
    String someResourceValue = "resource-value";

    //set up system property
    System.setProperty(someKey, someSystemPropertyValue);

    //set up config repo
    someProperties = new Properties();
    someProperties.setProperty(someKey, someLocalFileValue);
    someProperties.setProperty(anotherKey, someLocalFileValue);
    when(configRepository.getConfig()).thenReturn(someProperties);

    //set up resource file
    File resourceFile = new File(someResourceDir, someNamespace + ".properties");
    Files.write(someKey + "=" + someResourceValue, resourceFile, Charsets.UTF_8);
    Files.append(System.getProperty("line.separator"), resourceFile, Charsets.UTF_8);
    Files.append(anotherKey + "=" + someResourceValue, resourceFile, Charsets.UTF_8);
    Files.append(System.getProperty("line.separator"), resourceFile, Charsets.UTF_8);
    Files.append(lastKey + "=" + someResourceValue, resourceFile, Charsets.UTF_8);

    DefaultConfig defaultConfig =
        new DefaultConfig(someNamespace, configRepository);

    String someKeyValue = defaultConfig.getProperty(someKey, null);
    String anotherKeyValue = defaultConfig.getProperty(anotherKey, null);
    String lastKeyValue = defaultConfig.getProperty(lastKey, null);

    //clean up
    System.clearProperty(someKey);

    assertEquals(someSystemPropertyValue, someKeyValue);
    assertEquals(someLocalFileValue, anotherKeyValue);
    assertEquals(someResourceValue, lastKeyValue);

  }

  @Test
  public void testOnRepositoryChange() throws Exception {
    String someKey = "someKey";
    String someSystemPropertyValue = "system-property-value";

    String anotherKey = "anotherKey";
    String someLocalFileValue = "local-file-value";

    String keyToBeDeleted = "keyToBeDeleted";
    String keyToBeDeletedValue = "keyToBeDeletedValue";

    String yetAnotherKey = "yetAnotherKey";
    String yetAnotherValue = "yetAnotherValue";

    String yetAnotherResourceValue = "yetAnotherResourceValue";
    //set up system property
    System.setProperty(someKey, someSystemPropertyValue);

    //set up config repo
    someProperties = new Properties();
    someProperties.putAll(ImmutableMap
        .of(someKey, someLocalFileValue, anotherKey, someLocalFileValue, keyToBeDeleted,
            keyToBeDeletedValue, yetAnotherKey, yetAnotherValue));
    when(configRepository.getConfig()).thenReturn(someProperties);

    //set up resource file
    File resourceFile = new File(someResourceDir, someNamespace + ".properties");
    Files.append(yetAnotherKey + "=" + yetAnotherResourceValue, resourceFile, Charsets.UTF_8);

    DefaultConfig defaultConfig =
        new DefaultConfig(someNamespace, configRepository);

    ConfigChangeListener someListener = mock(ConfigChangeListener.class);
    defaultConfig.addChangeListener(someListener);

    Properties newProperties = new Properties();
    String someKeyNewValue = "new-some-value";
    String anotherKeyNewValue = "another-new-value";
    String newKey = "newKey";
    String newValue = "newValue";
    newProperties.putAll(ImmutableMap
        .of(someKey, someKeyNewValue, anotherKey, anotherKeyNewValue, newKey, newValue));

    final ArgumentCaptor<ConfigChangeEvent> captor =
        ArgumentCaptor.forClass(ConfigChangeEvent.class);

    defaultConfig.onRepositoryChange(someNamespace, newProperties);

    verify(someListener, times(1)).onChange(captor.capture());

    ConfigChangeEvent changeEvent = captor.getValue();

    assertEquals(someNamespace, changeEvent.getNamespace());
    assertEquals(4, changeEvent.getChanges().size());

    ConfigChange anotherKeyChange = changeEvent.getChange(anotherKey);
    assertEquals(someLocalFileValue, anotherKeyChange.getOldValue());
    assertEquals(anotherKeyNewValue, anotherKeyChange.getNewValue());
    assertEquals(PropertyChangeType.MODIFIED, anotherKeyChange.getChangeType());

    ConfigChange yetAnotherKeyChange = changeEvent.getChange(yetAnotherKey);
    assertEquals(yetAnotherValue, yetAnotherKeyChange.getOldValue());
    assertEquals(yetAnotherResourceValue, yetAnotherKeyChange.getNewValue());
    assertEquals(PropertyChangeType.MODIFIED, yetAnotherKeyChange.getChangeType());

    ConfigChange keyToBeDeletedChange = changeEvent.getChange(keyToBeDeleted);
    assertEquals(keyToBeDeletedValue, keyToBeDeletedChange.getOldValue());
    assertEquals(null, keyToBeDeletedChange.getNewValue());
    assertEquals(PropertyChangeType.DELETED, keyToBeDeletedChange.getChangeType());

    ConfigChange newKeyChange = changeEvent.getChange(newKey);
    assertEquals(null, newKeyChange.getOldValue());
    assertEquals(newValue, newKeyChange.getNewValue());
    assertEquals(PropertyChangeType.NEW, newKeyChange.getChangeType());
  }
}
