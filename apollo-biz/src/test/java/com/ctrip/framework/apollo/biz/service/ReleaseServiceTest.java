package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.AbstractUnitTest;
import com.ctrip.framework.apollo.biz.MockBeanFactory;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReleaseServiceTest extends AbstractUnitTest {

  @Mock
  private ReleaseRepository releaseRepository;
  @Mock
  private NamespaceService namespaceService;
  @Mock
  private ReleaseHistoryService releaseHistoryService;
  @Mock
  private ItemSetService itemSetService;
  @InjectMocks
  private ReleaseService releaseService;
  @Mock
  private ItemService itemService;

  private String appId = "appId-test";
  private String clusterName = "cluster-test";
  private String namespaceName = "namespace-test";
  private String user = "user-test";
  private long releaseId = 1;
  private Release firstRelease;
  private Release secondRelease;
  private PageRequest pageRequest;

  @Before
  public void init() {

    firstRelease = new Release();
    firstRelease.setId(releaseId);
    firstRelease.setAppId(appId);
    firstRelease.setClusterName(clusterName);
    firstRelease.setNamespaceName(namespaceName);
    firstRelease.setAbandoned(false);

    secondRelease = new Release();
    secondRelease.setAppId(appId);
    secondRelease.setClusterName(clusterName);
    secondRelease.setNamespaceName(namespaceName);
    secondRelease.setAbandoned(false);

    pageRequest = new PageRequest(0, 2);
  }

  @Test(expected = BadRequestException.class)
  public void testNamespaceNotExist() {

    when(releaseRepository.findOne(releaseId)).thenReturn(firstRelease);
    when(namespaceService.findOne(anyString(), anyString(), anyString())).thenReturn(new Namespace());

    releaseService.rollback(releaseId, user);
  }

  @Test(expected = BadRequestException.class)
  public void testHasNoRelease() {

    when(namespaceService.findOne(anyString(), anyString(), anyString())).thenReturn(new Namespace());
    when(releaseRepository.findOne(releaseId)).thenReturn(firstRelease);
    when(releaseRepository.findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
                                                                                                     clusterName,
                                                                                                     namespaceName,
                                                                                                     pageRequest))
        .thenReturn(null);

    releaseService.rollback(releaseId, user);
  }

  @Test
  public void testRollback() {

    when(namespaceService.findOne(anyString(), anyString(), anyString())).thenReturn(new Namespace());
    when(itemService.findItemsWithOrdered(anyString(), anyString(), anyString())).thenReturn(new ArrayList<>());
    when(releaseRepository.findOne(releaseId)).thenReturn(firstRelease);
    when(releaseRepository.findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
                                                                                                     clusterName,
                                                                                                     namespaceName,
                                                                                                     pageRequest))
        .thenReturn(
            Arrays.asList(firstRelease, secondRelease));

    releaseService.rollback(releaseId, user);

    verify(releaseRepository).save(firstRelease);
    Assert.assertEquals(true, firstRelease.isAbandoned());
    Assert.assertEquals(user, firstRelease.getDataChangeLastModifiedBy());
  }


  @Test
  public void testFindRelease() throws Exception {
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    long someReleaseId = 1;
    String someReleaseKey = "someKey";
    String someValidConfiguration = "{\"apollo.bar\": \"foo\"}";

    Release someRelease =
        MockBeanFactory.mockRelease(someReleaseId, someReleaseKey, someAppId, someClusterName,
                        someNamespaceName,
                        someValidConfiguration);

    when(releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(someAppId,
                                                                                                          someClusterName,
                                                                                                          someNamespaceName))
        .thenReturn(someRelease);

    Release result = releaseService.findLatestActiveRelease(someAppId, someClusterName, someNamespaceName);

    verify(releaseRepository, times(1))
        .findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(someAppId, someClusterName,
                                                                                        someNamespaceName);
    assertEquals(someAppId, result.getAppId());
    assertEquals(someClusterName, result.getClusterName());
    assertEquals(someReleaseId, result.getId());
    assertEquals(someReleaseKey, result.getReleaseKey());
    assertEquals(someValidConfiguration, result.getConfigurations());
  }

  @Test
  public void testLoadConfigWithConfigNotFound() throws Exception {
    String someAppId = "1";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";

    when(releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(someAppId,
                                                                                                          someClusterName,
                                                                                                          someNamespaceName))
        .thenReturn(null);

    Release result = releaseService.findLatestActiveRelease(someAppId, someClusterName, someNamespaceName);

    assertNull(result);
    verify(releaseRepository, times(1)).findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(
        someAppId, someClusterName, someNamespaceName);
  }

  @Test
  public void testFindByReleaseIds() throws Exception {
    Release someRelease = mock(Release.class);
    Release anotherRelease = mock(Release.class);
    long someReleaseId = 1;
    long anotherReleaseId = 2;
    List<Release> someReleases = Lists.newArrayList(someRelease, anotherRelease);
    Set<Long> someReleaseIds = Sets.newHashSet(someReleaseId, anotherReleaseId);

    when(releaseRepository.findAll(someReleaseIds)).thenReturn(someReleases);

    List<Release> result = releaseService.findByReleaseIds(someReleaseIds);

    assertEquals(someReleases, result);
  }

  @Test
  public void testFindByReleaseKeys() throws Exception {
    Release someRelease = mock(Release.class);
    Release anotherRelease = mock(Release.class);
    String someReleaseKey = "key1";
    String anotherReleaseKey = "key2";
    List<Release> someReleases = Lists.newArrayList(someRelease, anotherRelease);
    Set<String> someReleaseKeys = Sets.newHashSet(someReleaseKey, anotherReleaseKey);

    when(releaseRepository.findByReleaseKeyIn(someReleaseKeys)).thenReturn(someReleases);

    List<Release> result = releaseService.findByReleaseKeys(someReleaseKeys);

    assertEquals(someReleases, result);
  }


}
