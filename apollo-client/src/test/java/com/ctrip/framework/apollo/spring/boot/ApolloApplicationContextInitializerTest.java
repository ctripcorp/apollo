package com.ctrip.framework.apollo.spring.boot;

import com.ctrip.framework.apollo.core.ConfigConsts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApolloApplicationContextInitializerTest {

  private ApolloApplicationContextInitializer apolloApplicationContextInitializer;

  @Before
  public void setUp() throws Exception {
    apolloApplicationContextInitializer = new ApolloApplicationContextInitializer();
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("app.id");
    System.clearProperty(ConfigConsts.APOLLO_CLUSTER_KEY);
    System.clearProperty("apollo.cacheDir");
    System.clearProperty(ConfigConsts.APOLLO_META_KEY);
  }

  @Test
  public void testFillFromEnvironment() throws Exception {
    String someAppId = "someAppId";
    String someCluster = "someCluster";
    String someCacheDir = "someCacheDir";
    String someApolloMeta = "someApolloMeta";

    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    when(environment.getProperty("app.id")).thenReturn(someAppId);
    when(environment.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY)).thenReturn(someCluster);
    when(environment.getProperty("apollo.cacheDir")).thenReturn(someCacheDir);
    when(environment.getProperty(ConfigConsts.APOLLO_META_KEY)).thenReturn(someApolloMeta);

    apolloApplicationContextInitializer.initializeSystemProperty(environment);

    assertEquals(someAppId, System.getProperty("app.id"));
    assertEquals(someCluster, System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY));
    assertEquals(someCacheDir, System.getProperty("apollo.cacheDir"));
    assertEquals(someApolloMeta, System.getProperty(ConfigConsts.APOLLO_META_KEY));
  }

  @Test
  public void testFillFromEnvironmentWithSystemPropertyAlreadyFilled() throws Exception {
    String someAppId = "someAppId";
    String someCluster = "someCluster";
    String someCacheDir = "someCacheDir";
    String someApolloMeta = "someApolloMeta";

    System.setProperty("app.id", someAppId);
    System.setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, someCluster);
    System.setProperty("apollo.cacheDir", someCacheDir);
    System.setProperty(ConfigConsts.APOLLO_META_KEY, someApolloMeta);

    String anotherAppId = "anotherAppId";
    String anotherCluster = "anotherCluster";
    String anotherCacheDir = "anotherCacheDir";
    String anotherApolloMeta = "anotherApolloMeta";

    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    when(environment.getProperty("app.id")).thenReturn(anotherAppId);
    when(environment.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY)).thenReturn(anotherCluster);
    when(environment.getProperty("apollo.cacheDir")).thenReturn(anotherCacheDir);
    when(environment.getProperty(ConfigConsts.APOLLO_META_KEY)).thenReturn(anotherApolloMeta);

    apolloApplicationContextInitializer.initializeSystemProperty(environment);

    assertEquals(someAppId, System.getProperty("app.id"));
    assertEquals(someCluster, System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY));
    assertEquals(someCacheDir, System.getProperty("apollo.cacheDir"));
    assertEquals(someApolloMeta, System.getProperty(ConfigConsts.APOLLO_META_KEY));
  }

  @Test
  public void testFillFromEnvironmentWithNoPropertyFromEnvironment() throws Exception {
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    apolloApplicationContextInitializer.initializeSystemProperty(environment);

    assertNull(System.getProperty("app.id"));
    assertNull(System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY));
    assertNull(System.getProperty("apollo.cacheDir"));
    assertNull(System.getProperty(ConfigConsts.APOLLO_META_KEY));
  }
}
