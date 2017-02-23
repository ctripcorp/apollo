package com.ctrip.framework.apollo.configservice.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class AppNamespaceServiceWithCacheTest {
  private AppNamespaceServiceWithCache appNamespaceServiceWithCache;
  @Mock
  private AppNamespaceRepository appNamespaceRepository;

  @Mock
  private BizConfig bizConfig;

  private int scanInterval;
  private TimeUnit scanIntervalTimeUnit;
  private Comparator<AppNamespace> appNamespaceComparator = (o1, o2) -> (int) (o1.getId() -
      o2.getId());

  @Before
  public void setUp() throws Exception {
    appNamespaceServiceWithCache = new AppNamespaceServiceWithCache();
    ReflectionTestUtils.setField(appNamespaceServiceWithCache, "appNamespaceRepository",
        appNamespaceRepository);
    ReflectionTestUtils.setField(appNamespaceServiceWithCache, "bizConfig", bizConfig);

    scanInterval = 10;
    scanIntervalTimeUnit = TimeUnit.MILLISECONDS;
    when(bizConfig.appNamespaceCacheRebuildInterval()).thenReturn(scanInterval);
    when(bizConfig.appNamespaceCacheRebuildIntervalTimeUnit()).thenReturn(scanIntervalTimeUnit);
    when(bizConfig.appNamespaceCacheScanInterval()).thenReturn(scanInterval);
    when(bizConfig.appNamespaceCacheScanIntervalTimeUnit()).thenReturn(scanIntervalTimeUnit);
  }

  @Test
  public void testAppNamespace() throws Exception {
    String someAppId = "someAppId";
    String somePrivateNamespace = "somePrivateNamespace";
    long somePrivateNamespaceId = 1;
    String yetAnotherPrivateNamespace = "anotherPrivateNamespace";
    long yetAnotherPrivateNamespaceId = 4;
    String anotherPublicNamespace = "anotherPublicNamespace";
    long anotherPublicNamespaceId = 5;

    String somePublicAppId = "somePublicAppId";
    String somePublicNamespace = "somePublicNamespace";
    long somePublicNamespaceId = 2;
    String anotherPrivateNamespace = "anotherPrivateNamespace";
    long anotherPrivateNamespaceId = 3;

    int sleepInterval = scanInterval * 10;

    AppNamespace somePrivateAppNamespace = assembleAppNamespace(somePrivateNamespaceId,
        someAppId, somePrivateNamespace, false);
    AppNamespace somePublicAppNamespace = assembleAppNamespace(somePublicNamespaceId,
        somePublicAppId, somePublicNamespace, true);
    AppNamespace anotherPrivateAppNamespace = assembleAppNamespace(anotherPrivateNamespaceId,
        somePublicAppId, anotherPrivateNamespace, false);
    AppNamespace yetAnotherPrivateAppNamespace = assembleAppNamespace
        (yetAnotherPrivateNamespaceId, someAppId, yetAnotherPrivateNamespace, false);
    AppNamespace anotherPublicAppNamespace = assembleAppNamespace(anotherPublicNamespaceId,
        someAppId, anotherPublicNamespace, true);

    Set<String> someAppIdNamespaces = Sets.newHashSet
        (somePrivateNamespace, yetAnotherPrivateNamespace, anotherPublicNamespace);
    Set<String> somePublicAppIdNamespaces = Sets.newHashSet(somePublicNamespace,
        anotherPrivateNamespace);
    Set<String> publicNamespaces = Sets.newHashSet(somePublicNamespace, anotherPublicNamespace);

    List<Long> appNamespaceIds = Lists.newArrayList(somePrivateNamespaceId,
        somePublicNamespaceId, anotherPrivateNamespaceId, yetAnotherPrivateNamespaceId,
        anotherPublicNamespaceId);
    List<AppNamespace> allAppNamespaces = Lists.newArrayList(somePrivateAppNamespace,
        somePublicAppNamespace, anotherPrivateAppNamespace, yetAnotherPrivateAppNamespace,
        anotherPublicAppNamespace);

    // Test init
    appNamespaceServiceWithCache.afterPropertiesSet();

    // Should have no record now
    assertTrue(appNamespaceServiceWithCache.findByAppIdAndNamespaces(someAppId, someAppIdNamespaces)
        .isEmpty());
    assertTrue(appNamespaceServiceWithCache.findByAppIdAndNamespaces(somePublicAppId,
        somePublicAppIdNamespaces).isEmpty());
    assertTrue(appNamespaceServiceWithCache.findPublicNamespacesByNames(publicNamespaces).isEmpty
        ());

    // Add 1 private namespace and 1 public namespace
    when(appNamespaceRepository.findFirst500ByIdGreaterThanOrderByIdAsc(0)).thenReturn(Lists
        .newArrayList(somePrivateAppNamespace, somePublicAppNamespace));
    when(appNamespaceRepository.findAll(Lists.newArrayList(somePrivateNamespaceId,
        somePublicNamespaceId))).thenReturn(Lists.newArrayList(somePrivateAppNamespace,
        somePublicAppNamespace));

    scanIntervalTimeUnit.sleep(sleepInterval);

    check(Lists.newArrayList(somePrivateAppNamespace), appNamespaceServiceWithCache
        .findByAppIdAndNamespaces(someAppId, someAppIdNamespaces));
    check(Lists.newArrayList(somePublicAppNamespace), appNamespaceServiceWithCache
        .findByAppIdAndNamespaces(somePublicAppId, somePublicAppIdNamespaces));
    check(Lists.newArrayList(somePublicAppNamespace), appNamespaceServiceWithCache
        .findPublicNamespacesByNames(publicNamespaces));

    // Add 2 private namespaces and 1 public namespace
    when(appNamespaceRepository.findFirst500ByIdGreaterThanOrderByIdAsc(somePublicNamespaceId))
        .thenReturn(Lists.newArrayList(anotherPrivateAppNamespace, yetAnotherPrivateAppNamespace,
            anotherPublicAppNamespace));
    when(appNamespaceRepository.findAll(appNamespaceIds)).thenReturn(allAppNamespaces);

    scanIntervalTimeUnit.sleep(sleepInterval);

    check(Lists.newArrayList(somePrivateAppNamespace, yetAnotherPrivateAppNamespace,
        anotherPublicAppNamespace), appNamespaceServiceWithCache.findByAppIdAndNamespaces
        (someAppId, someAppIdNamespaces));
    check(Lists.newArrayList(somePublicAppNamespace, anotherPrivateAppNamespace),
        appNamespaceServiceWithCache.findByAppIdAndNamespaces(somePublicAppId,
            somePublicAppIdNamespaces));
    check(Lists.newArrayList(somePublicAppNamespace, anotherPublicAppNamespace),
        appNamespaceServiceWithCache.findPublicNamespacesByNames(publicNamespaces));

    // Update name
    String somePrivateNamespaceNew = "somePrivateNamespaceNew";
    AppNamespace somePrivateAppNamespaceNew = assembleAppNamespace(somePrivateAppNamespace.getId
        (), somePrivateAppNamespace.getAppId(), somePrivateNamespaceNew, somePrivateAppNamespace
        .isPublic());
    somePrivateAppNamespaceNew.setDataChangeLastModifiedTime(newDateWithDelta
        (somePrivateAppNamespace.getDataChangeLastModifiedTime(), 1));

    // Update appId
    String someAppIdNew = "someAppIdNew";
    AppNamespace yetAnotherPrivateAppNamespaceNew = assembleAppNamespace
        (yetAnotherPrivateAppNamespace.getId(), someAppIdNew, yetAnotherPrivateAppNamespace
            .getName(), false);
    yetAnotherPrivateAppNamespaceNew.setDataChangeLastModifiedTime(newDateWithDelta
        (yetAnotherPrivateAppNamespace.getDataChangeLastModifiedTime(), 1));

    // Update isPublic
    AppNamespace somePublicAppNamespaceNew = assembleAppNamespace(somePublicAppNamespace
            .getId(), somePublicAppNamespace.getAppId(), somePublicAppNamespace.getName(),
        !somePublicAppNamespace.isPublic());
    somePublicAppNamespaceNew.setDataChangeLastModifiedTime(newDateWithDelta
        (somePublicAppNamespace.getDataChangeLastModifiedTime(), 1));

    // Delete 1 private and 1 public
    when(appNamespaceRepository.findAll(appNamespaceIds)).thenReturn(Lists.newArrayList
        (somePrivateAppNamespaceNew, yetAnotherPrivateAppNamespaceNew, somePublicAppNamespaceNew));

    scanIntervalTimeUnit.sleep(sleepInterval);

    check(Collections.emptyList(), appNamespaceServiceWithCache
        .findByAppIdAndNamespaces(someAppId, someAppIdNamespaces));
    check(Lists.newArrayList(somePublicAppNamespaceNew),
        appNamespaceServiceWithCache.findByAppIdAndNamespaces(somePublicAppId,
            somePublicAppIdNamespaces));
    check(Collections.emptyList(),
        appNamespaceServiceWithCache.findPublicNamespacesByNames(publicNamespaces));

    check(Lists.newArrayList(somePrivateAppNamespaceNew), appNamespaceServiceWithCache
        .findByAppIdAndNamespaces(someAppId, Sets.newHashSet(somePrivateNamespaceNew)));
    check(Lists.newArrayList(yetAnotherPrivateAppNamespaceNew), appNamespaceServiceWithCache
        .findByAppIdAndNamespaces(someAppIdNew, Sets.newHashSet(yetAnotherPrivateNamespace)));
  }

  private void check(List<AppNamespace> someList, List<AppNamespace> anotherList) {
    Collections.sort(someList, appNamespaceComparator);
    Collections.sort(anotherList, appNamespaceComparator);
    assertEquals(someList, anotherList);
  }

  private Date newDateWithDelta(Date date, int deltaInSeconds) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, deltaInSeconds);

    return calendar.getTime();
  }

  private AppNamespace assembleAppNamespace(long id, String appId, String name, boolean isPublic) {
    AppNamespace appNamespace = new AppNamespace();
    appNamespace.setId(id);
    appNamespace.setAppId(appId);
    appNamespace.setName(name);
    appNamespace.setPublic(isPublic);
    appNamespace.setDataChangeLastModifiedTime(new Date());
    return appNamespace;
  }
}