package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.AbstractUnitTest;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class NamespacePublishInfoTest extends AbstractUnitTest {

  @Mock
  private ClusterService clusterService;
  @Mock
  private ReleaseService releaseService;
  @Mock
  private ItemService itemService;
  @Mock
  private NamespaceRepository namespaceRepository;

  @InjectMocks
  private NamespaceService namespaceService;

  private String testApp = "testApp";

  @Test
  public void testNamespaceNotEverPublishedButHasItems() {
    Cluster cluster = createCluster(ConfigConsts.CLUSTER_NAME_DEFAULT);
    Namespace namespace = createNamespace(ConfigConsts.CLUSTER_NAME_DEFAULT, ConfigConsts.NAMESPACE_APPLICATION);
    Item item = createItem(namespace.getId(), "a", "b");

    when(clusterService.findParentClusters(testApp)).thenReturn(Collections.singletonList(cluster));
    when(namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(testApp, ConfigConsts.CLUSTER_NAME_DEFAULT))
        .thenReturn(Collections.singletonList(namespace));
    when(itemService.findLastOne(anyLong())).thenReturn(item);

    Map<String, Boolean> result = namespaceService.namespacePublishInfo(testApp);

    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.get(ConfigConsts.CLUSTER_NAME_DEFAULT));
  }

  @Test
  public void testNamespaceEverPublishedAndNotModifiedAfter() {
    Cluster cluster = createCluster(ConfigConsts.CLUSTER_NAME_DEFAULT);
    Namespace namespace = createNamespace(ConfigConsts.CLUSTER_NAME_DEFAULT, ConfigConsts.NAMESPACE_APPLICATION);
    Item item = createItem(namespace.getId(), "a", "b");
    Release release = createRelease("{\"a\":\"b\"}");

    when(clusterService.findParentClusters(testApp)).thenReturn(Collections.singletonList(cluster));
    when(namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(testApp, ConfigConsts.CLUSTER_NAME_DEFAULT))
        .thenReturn(Collections.singletonList(namespace));
    when(releaseService.findLatestActiveRelease(namespace)).thenReturn(release);
    when(itemService.findItemsModifiedAfterDate(anyLong(), anyObject())).thenReturn(Collections.singletonList(item));

    Map<String, Boolean> result = namespaceService.namespacePublishInfo(testApp);

    Assert.assertEquals(1, result.size());
    Assert.assertFalse(result.get(ConfigConsts.CLUSTER_NAME_DEFAULT));

  }

  @Test
  public void testNamespaceEverPublishedAndModifiedAfter() {
    Cluster cluster = createCluster(ConfigConsts.CLUSTER_NAME_DEFAULT);
    Namespace namespace = createNamespace(ConfigConsts.CLUSTER_NAME_DEFAULT, ConfigConsts.NAMESPACE_APPLICATION);
    Item item = createItem(namespace.getId(), "a", "b");
    Release release = createRelease("{\"a\":\"c\"}");

    when(clusterService.findParentClusters(testApp)).thenReturn(Collections.singletonList(cluster));
    when(namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(testApp, ConfigConsts.CLUSTER_NAME_DEFAULT))
        .thenReturn(Collections.singletonList(namespace));
    when(releaseService.findLatestActiveRelease(namespace)).thenReturn(release);
    when(itemService.findItemsModifiedAfterDate(anyLong(), anyObject())).thenReturn(Collections.singletonList(item));

    Map<String, Boolean> result = namespaceService.namespacePublishInfo(testApp);

    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.get(ConfigConsts.CLUSTER_NAME_DEFAULT));

  }

  private Cluster createCluster(String clusterName) {
    Cluster cluster = new Cluster();

    cluster.setAppId(testApp);
    cluster.setName(clusterName);
    cluster.setParentClusterId(0);

    return cluster;
  }

  private Namespace createNamespace(String clusterName, String namespaceName) {
    Namespace namespace = new Namespace();

    namespace.setAppId(testApp);
    namespace.setClusterName(clusterName);
    namespace.setNamespaceName(namespaceName);
    namespace.setId(new Random().nextLong());

    return namespace;
  }

  private Item createItem(long namespaceId, String key, String value) {
    Item item = new Item();

    item.setNamespaceId(namespaceId);
    item.setKey(key);
    item.setValue(value);

    return item;
  }

  private Release createRelease(String configuration) {
    Release release = new Release();
    release.setConfigurations(configuration);
    return release;
  }

}
