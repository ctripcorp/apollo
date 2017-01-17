package com.ctrip.framework.apollo.configservice.integration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import com.ctrip.framework.apollo.configservice.service.ReleaseMessageServiceWithCache;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class NotificationControllerV2IntegrationTest extends AbstractBaseIntegrationTest {
  @Autowired
  private Gson gson;

  @Autowired
  private ReleaseMessageServiceWithCache releaseMessageServiceWithCache;

  private String someAppId;
  private String someCluster;
  private String defaultNamespace;
  private String somePublicNamespace;
  private ExecutorService executorService;
  private ParameterizedTypeReference<List<ApolloConfigNotification>> typeReference;

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.invokeMethod(releaseMessageServiceWithCache, "reset");
    someAppId = "someAppId";
    someCluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    defaultNamespace = ConfigConsts.NAMESPACE_APPLICATION;
    somePublicNamespace = "somePublicNamespace";
    executorService = Executors.newSingleThreadExecutor();
    typeReference = new ParameterizedTypeReference<List<ApolloConfigNotification>>() {
    };
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithDefaultNamespace() throws Exception {
    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(someAppId, someCluster, defaultNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(defaultNamespace, -1));

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(defaultNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithDefaultNamespaceAsFile() throws Exception {
    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(someAppId, someCluster, defaultNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(defaultNamespace + ".properties", -1));

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(defaultNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithMultipleNamespaces() throws Exception {
    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(someAppId, someCluster, somePublicNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(defaultNamespace + ".properties", -1,
            defaultNamespace, -1, somePublicNamespace, -1));

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithPrivateNamespaceAsFile() throws Exception {
    String namespace = "someNamespace.xml";
    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService,
        assembleKey(someAppId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(namespace, -1));

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(namespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/test-release-message.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithDefaultNamespaceWithNotificationIdOutDated()
      throws Exception {
    long someOutDatedNotificationId = 1;
    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(defaultNamespace, someOutDatedNotificationId));

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(defaultNamespace, notifications.get(0).getNamespaceName());
    assertEquals(10, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWthPublicNamespaceAndNoDataCenter() throws Exception {
    String publicAppId = "somePublicAppId";

    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService,
        assembleKey(publicAppId, ConfigConsts.CLUSTER_NAME_DEFAULT, somePublicNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(somePublicNamespace, -1));

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWthPublicNamespaceAndDataCenter() throws Exception {
    String publicAppId = "somePublicAppId";
    String someDC = "someDC";

    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(publicAppId, someDC, somePublicNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}&dataCenter={dataCenter}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(somePublicNamespace, -1), someDC);

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWthMultipleNamespacesAndMultipleNamespacesChanged()
      throws Exception {
    String publicAppId = "somePublicAppId";
    String someDC = "someDC";

    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(publicAppId, someDC, somePublicNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}&dataCenter={dataCenter}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(defaultNamespace, -1, somePublicNamespace, -1),
        someDC);

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWthPublicNamespaceAsFile() throws Exception {
    String publicAppId = "somePublicAppId";
    String someDC = "someDC";

    AtomicBoolean stop = new AtomicBoolean();
    periodicSendMessage(executorService, assembleKey(publicAppId, someDC, somePublicNamespace),
        stop);

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}&dataCenter={dataCenter}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(somePublicNamespace + ".properties", -1),
        someDC);

    stop.set(true);

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/test-release-message.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithPublicNamespaceWithNotificationIdOutDated() throws Exception {
    long someOutDatedNotificationId = 1;

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(somePublicNamespace,
            someOutDatedNotificationId));

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, notifications.size());
    assertEquals(somePublicNamespace, notifications.get(0).getNamespaceName());
    assertNotEquals(0, notifications.get(0).getNotificationId());
  }

  @Test(timeout = 5000L)
  @Sql(scripts = "/integration-test/test-release.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/test-release-message.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/integration-test/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testPollNotificationWithMultipleNamespacesAndNotificationIdsOutDated()
      throws Exception {
    long someOutDatedNotificationId = 1;

    ResponseEntity<List<ApolloConfigNotification>> result = restTemplate.exchange(
        "{baseurl}/notifications/v2?appId={appId}&cluster={clusterName}&notifications={notifications}",
        HttpMethod.GET, null, typeReference,
        getHostUrl(), someAppId, someCluster,
        transformApolloConfigNotificationsToString(somePublicNamespace,
            someOutDatedNotificationId, defaultNamespace, someOutDatedNotificationId));

    List<ApolloConfigNotification> notifications = result.getBody();
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(2, notifications.size());

    Set<String> outDatedNamespaces =
        Sets.newHashSet(notifications.get(0).getNamespaceName(),
            notifications.get(1).getNamespaceName());
    assertEquals(Sets.newHashSet(defaultNamespace, somePublicNamespace), outDatedNamespaces);
    assertNotEquals(0, notifications.get(0).getNotificationId());
    assertNotEquals(1, notifications.get(1).getNotificationId());
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).join(appId, cluster, namespace);
  }

  private String transformApolloConfigNotificationsToString(
      String namespace, long notificationId) {
    List<ApolloConfigNotification> notifications =
        Lists.newArrayList(assembleApolloConfigNotification(namespace, notificationId));
    return gson.toJson(notifications);
  }

  private String transformApolloConfigNotificationsToString(String namespace, long notificationId,
                                                            String anotherNamespace,
                                                            long anotherNotificationId) {
    List<ApolloConfigNotification> notifications =
        Lists.newArrayList(assembleApolloConfigNotification(namespace, notificationId),
            assembleApolloConfigNotification(anotherNamespace, anotherNotificationId));
    return gson.toJson(notifications);
  }

  private String transformApolloConfigNotificationsToString(String namespace, long notificationId,
                                                            String anotherNamespace,
                                                            long anotherNotificationId,
                                                            String yetAnotherNamespace,
                                                            long yetAnotherNotificationId) {
    List<ApolloConfigNotification> notifications =
        Lists.newArrayList(assembleApolloConfigNotification(namespace, notificationId),
            assembleApolloConfigNotification(anotherNamespace, anotherNotificationId),
            assembleApolloConfigNotification(yetAnotherNamespace, yetAnotherNotificationId));
    return gson.toJson(notifications);
  }

  private ApolloConfigNotification assembleApolloConfigNotification(String namespace,
                                                                    long notificationId) {
    ApolloConfigNotification notification = new ApolloConfigNotification();
    notification.setNamespaceName(namespace);
    notification.setNotificationId(notificationId);
    return notification;
  }
}
