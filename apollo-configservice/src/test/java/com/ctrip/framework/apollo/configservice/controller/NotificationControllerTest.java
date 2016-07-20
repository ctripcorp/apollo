package com.ctrip.framework.apollo.configservice.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.service.ReleaseMessageService;
import com.ctrip.framework.apollo.biz.utils.EntityManagerUtil;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationControllerTest {
  private NotificationController controller;
  private String someAppId;
  private String someCluster;
  private String defaultCluster;
  private String defaultNamespace;
  private String somePublicNamespace;
  private String someDataCenter;
  private long someNotificationId;
  private String someClientIp;
  @Mock
  private ReleaseMessageService releaseMessageService;
  @Mock
  private EntityManagerUtil entityManagerUtil;
  @Mock
  private NamespaceUtil namespaceUtil;
  @Mock
  private WatchKeysUtil watchKeysUtil;

  private Multimap<String, DeferredResult<ResponseEntity<ApolloConfigNotification>>>
      deferredResults;

  @Before
  public void setUp() throws Exception {
    controller = new NotificationController();
    ReflectionTestUtils.setField(controller, "releaseMessageService", releaseMessageService);
    ReflectionTestUtils.setField(controller, "entityManagerUtil", entityManagerUtil);
    ReflectionTestUtils.setField(controller, "namespaceUtil", namespaceUtil);
    ReflectionTestUtils.setField(controller, "watchKeysUtil", watchKeysUtil);

    someAppId = "someAppId";
    someCluster = "someCluster";
    defaultCluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    defaultNamespace = ConfigConsts.NAMESPACE_APPLICATION;
    somePublicNamespace = "somePublicNamespace";
    someDataCenter = "someDC";
    someNotificationId = 1;
    someClientIp = "someClientIp";

    when(namespaceUtil.filterNamespaceName(defaultNamespace)).thenReturn(defaultNamespace);
    when(namespaceUtil.filterNamespaceName(somePublicNamespace)).thenReturn(somePublicNamespace);

    deferredResults =
        (Multimap<String, DeferredResult<ResponseEntity<ApolloConfigNotification>>>) ReflectionTestUtils
            .getField(controller, "deferredResults");
  }

  @Test
  public void testPollNotificationWithDefaultNamespace() throws Exception {
    String someWatchKey = "someKey";
    String anotherWatchKey = "anotherKey";

    Set<String> watchKeys = Sets.newHashSet(someWatchKey, anotherWatchKey);

    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster, Sets.newHashSet(defaultNamespace),
            someDataCenter)).thenReturn(
        watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, defaultNamespace, someDataCenter,
            someNotificationId, someClientIp);

    assertEquals(watchKeys.size(), deferredResults.size());

    for (String watchKey : watchKeys) {
      assertTrue(deferredResults.get(watchKey).contains(deferredResult));
    }
  }

  @Test
  public void testPollNotificationWithDefaultNamespaceAsFile() throws Exception {
    String namespace = String.format("%s.%s", defaultNamespace, "properties");
    when(namespaceUtil.filterNamespaceName(namespace)).thenReturn(defaultNamespace);

    String someWatchKey = "someKey";
    String anotherWatchKey = "anotherKey";

    Set<String> watchKeys = Sets.newHashSet(someWatchKey, anotherWatchKey);

    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster, Sets.newHashSet(defaultNamespace),
            someDataCenter)).thenReturn(
        watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, namespace, someDataCenter,
            someNotificationId, someClientIp);

    assertEquals(watchKeys.size(), deferredResults.size());

    for (String watchKey : watchKeys) {
      assertTrue(deferredResults.get(watchKey).contains(deferredResult));
    }
  }

  @Test
  public void testPollNotificationWithSomeNamespaceAsFile() throws Exception {
    String namespace = String.format("someNamespace.xml");

    when(namespaceUtil.filterNamespaceName(namespace)).thenReturn(namespace);

    String someWatchKey = "someKey";

    Set<String> watchKeys = Sets.newHashSet(someWatchKey);
    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster, Sets.newHashSet(namespace), someDataCenter))
        .thenReturn(
            watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, namespace, someDataCenter,
            someNotificationId, someClientIp);

    assertEquals(watchKeys.size(), deferredResults.size());

    for (String watchKey : watchKeys) {
      assertTrue(deferredResults.get(watchKey).contains(deferredResult));
    }
  }

  @Test
  public void testPollNotificationWithDefaultNamespaceWithNotificationIdOutDated()
      throws Exception {
    long notificationId = someNotificationId + 1;
    String releaseMessage = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(someAppId, someCluster, defaultNamespace);
    ReleaseMessage someReleaseMessage = mock(ReleaseMessage.class);

    String someWatchKey = "someKey";

    Set<String> watchKeys = Sets.newHashSet(someWatchKey);
    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster, Sets.newHashSet(defaultNamespace),
            someDataCenter))
        .thenReturn(
            watchKeys);

    when(someReleaseMessage.getId()).thenReturn(notificationId);
    when(someReleaseMessage.getMessage()).thenReturn(releaseMessage);
    when(releaseMessageService.findLatestReleaseMessageForMessages(watchKeys))
        .thenReturn(someReleaseMessage);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, defaultNamespace, someDataCenter,
            someNotificationId, someClientIp);

    ResponseEntity<ApolloConfigNotification> result =
        (ResponseEntity<ApolloConfigNotification>) deferredResult.getResult();

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(defaultNamespace, result.getBody().getNamespaceName());
    assertEquals(notificationId, result.getBody().getNotificationId());
  }

  @Test
  public void testPollNotificationWithDefaultNamespaceAndHandleMessage() throws Exception {
    String someWatchKey = "someKey";
    String anotherWatchKey = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(someAppId, someCluster, defaultNamespace);

    Set<String> watchKeys = Sets.newHashSet(someWatchKey, anotherWatchKey);

    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster, Sets.newHashSet(defaultNamespace),
            someDataCenter)).thenReturn(
        watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, defaultNamespace, someDataCenter,
            someNotificationId, someClientIp);

    long someId = 1;
    ReleaseMessage someReleaseMessage = new ReleaseMessage(anotherWatchKey);
    someReleaseMessage.setId(someId);

    controller.handleMessage(someReleaseMessage, Topics.APOLLO_RELEASE_TOPIC);

    ResponseEntity<ApolloConfigNotification> response =
        (ResponseEntity<ApolloConfigNotification>) deferredResult.getResult();
    ApolloConfigNotification notification = response.getBody();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(defaultNamespace, notification.getNamespaceName());
    assertEquals(someId, notification.getNotificationId());
  }

  @Test
  public void testPollNotificationWithMultipleNamespaces() throws Exception {
    String defaultNamespaceAsFile = defaultNamespace + ".properties";
    String somePublicNamespaceAsFile = somePublicNamespace + ".xml";
    String namespaces = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(defaultNamespace, defaultNamespaceAsFile, somePublicNamespace,
            somePublicNamespaceAsFile);

    when(namespaceUtil.filterNamespaceName(defaultNamespaceAsFile)).thenReturn(defaultNamespace);
    when(namespaceUtil.filterNamespaceName(somePublicNamespaceAsFile))
        .thenReturn(somePublicNamespaceAsFile);

    String someWatchKey = "someKey";
    String anotherWatchKey = "anotherKey";

    Set<String> watchKeys = Sets.newHashSet(someWatchKey, anotherWatchKey);

    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster,
            Sets.newHashSet(defaultNamespace, somePublicNamespace, somePublicNamespaceAsFile),
            someDataCenter)).thenReturn(
        watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, namespaces, someDataCenter,
            someNotificationId, someClientIp);

    assertEquals(watchKeys.size(), deferredResults.size());

    for (String watchKey : watchKeys) {
      assertTrue(deferredResults.get(watchKey).contains(deferredResult));
    }

    verify(watchKeysUtil, times(1)).assembleAllWatchKeys(someAppId, someCluster,
        Sets.newHashSet(defaultNamespace, somePublicNamespace, somePublicNamespaceAsFile),
        someDataCenter);

  }

  @Test
  public void testPollNotificationWithMultipleNamespacesAndHandleMessage() throws Exception {
    String namespaces = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(defaultNamespace, somePublicNamespace);

    String someWatchKey = "someKey";
    String anotherWatchKey = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
        .join(someAppId, someCluster, somePublicNamespace);

    Set<String> watchKeys = Sets.newHashSet(someWatchKey, anotherWatchKey);

    when(watchKeysUtil
        .assembleAllWatchKeys(someAppId, someCluster,
            Sets.newHashSet(defaultNamespace, somePublicNamespace), someDataCenter)).thenReturn(
        watchKeys);

    DeferredResult<ResponseEntity<ApolloConfigNotification>>
        deferredResult = controller
        .pollNotification(someAppId, someCluster, namespaces, someDataCenter,
            someNotificationId, someClientIp);

    assertEquals(watchKeys.size(), deferredResults.size());

    long someId = 1;
    ReleaseMessage someReleaseMessage = new ReleaseMessage(anotherWatchKey);
    someReleaseMessage.setId(someId);

    controller.handleMessage(someReleaseMessage, Topics.APOLLO_RELEASE_TOPIC);

    ResponseEntity<ApolloConfigNotification> response =
        (ResponseEntity<ApolloConfigNotification>) deferredResult.getResult();
    ApolloConfigNotification notification = response.getBody();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(somePublicNamespace, notification.getNamespaceName());
    assertEquals(someId, notification.getNotificationId());
  }
}
