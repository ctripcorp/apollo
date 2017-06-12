package com.ctrip.framework.apollo.configservice.service;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.utils.MockBeanFactory;
import com.ctrip.framework.apollo.configservice.internal.ConfigCache;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceTest {

  @Mock
  private GrayReleaseRulesHolder grayReleaseRulesHolder;
  @Mock
  private ReleaseService releaseService;
  @Mock
  private ConfigCache configCache;

  @InjectMocks
  private ConfigService configService;

  private String appId = "appId";
  private String clientIp = "1.1.1.1";
  private String targetCluster = "targetCluster";
  private String namespace = "namespace";
  private String dc = "dc";
  private String releaseKey = "releaseKey";
  private long releaseId = 1;

  @Test
  public void testLoadConfigFromSpecifiedCluster() {

    Release clusterRelease =
        MockBeanFactory.mockRelease(releaseId, releaseKey, appId, targetCluster, namespace, null);

    when(grayReleaseRulesHolder
             .findReleaseIdFromGrayReleaseRule(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(null);
    when(configCache.get(anyString(), anyString(), anyString()))
        .thenReturn(null);
    when(releaseService.findLatestActiveRelease(appId, targetCluster, namespace))
        .thenReturn(clusterRelease);

    Release loadedRelease = configService.loadConfig(appId, clientIp, appId, targetCluster, namespace, dc);

    Assert.assertEquals(releaseKey, loadedRelease.getReleaseKey());
    verify(grayReleaseRulesHolder, times(0))
        .findReleaseIdFromGrayReleaseRule(appId, clientIp, appId, dc, namespace);
  }

  @Test
  public void testLoadConfigFromDcCluster() {

    Release result =
        MockBeanFactory.mockRelease(releaseId, releaseKey, appId, targetCluster, namespace, null);

    when(grayReleaseRulesHolder
             .findReleaseIdFromGrayReleaseRule(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(null);
    when(configCache.get(anyString(), anyString(), anyString()))
        .thenReturn(null);
    when(releaseService.findLatestActiveRelease(appId, dc, namespace))
        .thenReturn(result);

    Release loadedRelease =
        configService.loadConfig(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, dc);

    verify(grayReleaseRulesHolder, times(0))
        .findReleaseIdFromGrayReleaseRule(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT,
                                          namespace);
    verify(releaseService).findLatestActiveRelease(appId, dc, namespace);
    Assert.assertEquals(releaseKey, loadedRelease.getReleaseKey());
  }

  @Test
  public void testLoadConfigFromDefaultCluster() {

    Release result = MockBeanFactory
            .mockRelease(releaseId, releaseKey, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, null);

    when(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(anyString(), anyString(), anyString(),
                                                                 anyString(), anyString())).thenReturn(null);
    when(configCache.get(anyString(), anyString(), anyString()))
        .thenReturn(null);
    when(releaseService.findLatestActiveRelease(appId, dc, namespace))
        .thenReturn(null);
    when(releaseService.findLatestActiveRelease(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace))
        .thenReturn(result);

    Release loadedRelease =
        configService.loadConfig(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, dc);


    verify(grayReleaseRulesHolder, times(2)).findReleaseIdFromGrayReleaseRule(anyString(), anyString(), anyString(),
                                                                              anyString(), anyString());
    verify(releaseService).findLatestActiveRelease(appId, dc, namespace);
    verify(releaseService).findLatestActiveRelease(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
    Assert.assertEquals(releaseKey, loadedRelease.getReleaseKey());
  }

  @Test
  public void testLoadConfigHitGrayRelease() {
    Release result = MockBeanFactory
        .mockRelease(releaseId, releaseKey, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, null);

    when(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(appId, clientIp, appId, dc, namespace))
        .thenReturn(null);
    when(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace))
        .thenReturn(releaseId);
    when(releaseService.findActiveOne(releaseId)).thenReturn(result);

    Release loadedRelease =
        configService.loadConfig(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, dc);

    verify(releaseService).findLatestActiveRelease(appId, dc, namespace);
    verify(releaseService, never()).findLatestActiveRelease(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
    verify(releaseService).findActiveOne(releaseId);
    Assert.assertEquals(releaseKey, loadedRelease.getReleaseKey());
  }

  @Test
  public void testLoadConfigHitCache() {
    Release result = MockBeanFactory
        .mockRelease(releaseId, releaseKey, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, null);

    when(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(appId, clientIp, appId, dc, namespace))
        .thenReturn(null);
    when(configCache.get(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace)).thenReturn(result);

    Release loadedRelease =
        configService.loadConfig(appId, clientIp, appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace, dc);

    verify(releaseService).findLatestActiveRelease(appId, dc, namespace);
    verify(releaseService, never()).findLatestActiveRelease(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
    verify(releaseService, never()).findActiveOne(releaseId);
    verify(configCache).get(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
    verify(configCache).get(appId, dc, namespace);
    Assert.assertEquals(releaseKey, loadedRelease.getReleaseKey());
  }
}
