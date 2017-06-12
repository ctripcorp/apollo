package com.ctrip.framework.apollo.biz.service;

import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.biz.AbstractUnitTest;
import com.ctrip.framework.apollo.biz.utils.MockBeanFactory;
import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import com.ctrip.framework.apollo.biz.repository.ServerConfigRepository;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class BizDBPropertySourceTest extends AbstractUnitTest {

  @Mock
  private ServerConfigRepository serverConfigRepository;
  private BizDBPropertySource propertySource;

  private String clusterConfigKey = "clusterKey";
  private String clusterConfigValue = "clusterValue";
  private String dcConfigKey = "dcKey";
  private String dcConfigValue = "dcValue";
  private String defaultKey = "defaultKey";
  private String defaultValue = "defaultValue";

  @Before
  public void initTestData() {
    propertySource = spy(new BizDBPropertySource());
    ReflectionTestUtils.setField(propertySource, "serverConfigRepository", serverConfigRepository);

    List<ServerConfig> configs = Lists.newLinkedList();

    //cluster config
    String cluster = "cluster";
    configs.add(MockBeanFactory.mockServerConfig(clusterConfigKey, clusterConfigValue, cluster));
    String dc = "dc";
    configs.add(MockBeanFactory.mockServerConfig(clusterConfigKey, clusterConfigValue + "dc", dc));
    configs.add(MockBeanFactory.mockServerConfig(clusterConfigKey, clusterConfigValue + ConfigConsts.CLUSTER_NAME_DEFAULT,
                                   ConfigConsts.CLUSTER_NAME_DEFAULT));

    //dc config
    configs.add(MockBeanFactory.mockServerConfig(dcConfigKey, dcConfigValue, dc));
    configs.add(MockBeanFactory.mockServerConfig(dcConfigKey, dcConfigValue + ConfigConsts.CLUSTER_NAME_DEFAULT,
                                   ConfigConsts.CLUSTER_NAME_DEFAULT));

    //default config
    configs.add(MockBeanFactory.mockServerConfig(defaultKey, defaultValue, ConfigConsts.CLUSTER_NAME_DEFAULT));

    System.setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, cluster);

    when(propertySource.getCurrentDataCenter()).thenReturn(dc);
    when(serverConfigRepository.findAll()).thenReturn(configs);
  }

  @After
  public void clear() {
    System.clearProperty(ConfigConsts.APOLLO_CLUSTER_KEY);
  }

  @Test
  public void testGetClusterConfig() {

    propertySource.refresh();

    assertEquals(propertySource.getProperty(clusterConfigKey), clusterConfigValue);
  }

  @Test
  public void testGetDcConfig() {
    propertySource.refresh();

    assertEquals(propertySource.getProperty(dcConfigKey), dcConfigValue);
  }

  @Test
  public void testGetDefaultConfig() {
    propertySource.refresh();


    assertEquals(propertySource.getProperty(defaultKey), defaultValue);
  }

  @Test
  public void testGetNull() {
    propertySource.refresh();
    assertNull(propertySource.getProperty("noKey"));
  }


}
