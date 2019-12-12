package com.ctrip.framework.apollo.internals;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

import java.util.LinkedHashMap;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TxtConfigFileTest {

  private String someNamespace;
  @Mock
  private ConfigRepository configRepository;

  @Before
  public void setUp() throws Exception {
    someNamespace = "someName";
  }

  @Test
  public void testWhenHasContent() throws Exception {
    LinkedHashMap someProperties = new LinkedHashMap();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    someProperties.put(key, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    TxtConfigFile configFile = new TxtConfigFile(someNamespace, configRepository);

    assertEquals(ConfigFileFormat.TXT, configFile.getConfigFileFormat());
    assertEquals(someNamespace, configFile.getNamespace());
    assertTrue(configFile.hasContent());
    assertEquals(someValue, configFile.getContent());
  }
}
