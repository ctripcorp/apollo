package com.ctrip.framework.apollo.configservice.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class WatchKeysUtilTest {
  @Mock
  private AppNamespaceService appNamespaceService;
  @Mock
  private AppNamespace someAppNamespace;
  @Mock
  private AppNamespace anotherAppNamespace;
  @Mock
  private AppNamespace somePublicAppNamespace;
  private WatchKeysUtil watchKeysUtil;
  private String someAppId;
  private String someCluster;
  private String someNamespace;
  private String anotherNamespace;
  private String somePublicNamespace;
  private String defaultCluster;
  private String someDC;
  private String somePublicAppId;

  @Before
  public void setUp() throws Exception {
    watchKeysUtil = new WatchKeysUtil();

    someAppId = "someId";
    someCluster = "someCluster";
    someNamespace = "someName";
    anotherNamespace = "anotherName";
    somePublicNamespace = "somePublicName";
    defaultCluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    someDC = "someDC";
    somePublicAppId = "somePublicId";

    when(someAppNamespace.getName()).thenReturn(someNamespace);
    when(anotherAppNamespace.getName()).thenReturn(anotherNamespace);
    when(appNamespaceService.findByAppIdAndNamespaces(someAppId, Sets.newHashSet(someNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace));
    when(appNamespaceService
        .findByAppIdAndNamespaces(someAppId, Sets.newHashSet(someNamespace, anotherNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace, anotherAppNamespace));
    when(appNamespaceService
        .findByAppIdAndNamespaces(someAppId,
            Sets.newHashSet(someNamespace, anotherNamespace, somePublicNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace, anotherAppNamespace));

    when(somePublicAppNamespace.getAppId()).thenReturn(somePublicAppId);
    when(somePublicAppNamespace.getName()).thenReturn(somePublicNamespace);
    when(appNamespaceService.findPublicNamespacesByNames(Sets.newHashSet(somePublicNamespace)))
        .thenReturn(Lists.newArrayList(somePublicAppNamespace));

    ReflectionTestUtils.setField(watchKeysUtil, "appNamespaceService", appNamespaceService);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndDefaultCluster() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, defaultCluster, someNamespace, null);

    Set<String> clusters = Sets.newHashSet(defaultCluster);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndSomeDC() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someDC, someNamespace, someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someDC);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndSomeDCAndSomeCluster() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster, someNamespace, someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithMultipleNamespaces() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster,
            Sets.newHashSet(someNamespace, anotherNamespace), someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size() * 2, watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
    assertWatchKeys(someAppId, clusters, anotherNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithPrivateAndPublicNamespaces() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster,
            Sets.newHashSet(someNamespace, anotherNamespace, somePublicNamespace), someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size() * 4, watchKeys.size());

    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
    assertWatchKeys(someAppId, clusters, anotherNamespace, watchKeys);
    assertWatchKeys(someAppId, clusters, somePublicNamespace, watchKeys);
    assertWatchKeys(somePublicAppId, clusters, somePublicNamespace, watchKeys);
  }

  private void assertWatchKeys(String appId, Set<String> clusters, String namespaceName,
                               Set<String> watchedKeys) {
    for (String cluster : clusters) {
      String key =
          Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
              .join(appId, cluster, namespaceName);
      assertTrue(watchedKeys.contains(key));
    }
  }
}
