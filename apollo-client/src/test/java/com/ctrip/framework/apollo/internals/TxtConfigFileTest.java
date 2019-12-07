package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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
    Properties someProperties = new Properties();
    String key = ConfigConsts.CONFIG_FILE_CONTENT_KEY;
    String someValue = "someValue";
    someProperties.setProperty(key, someValue);

    when(configRepository.getConfig()).thenReturn(someProperties);

    TxtConfigFile configFile = new TxtConfigFile(someNamespace, configRepository);

    assertEquals(ConfigFileFormat.TXT, configFile.getConfigFileFormat());
    assertEquals(someNamespace, configFile.getNamespace());
    assertTrue(configFile.hasContent());
    assertEquals(someValue, configFile.getContent());
  }
}
