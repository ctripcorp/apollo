package com.ctrip.framework.apollo.openapi.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class ApolloOpenApiClientTest {

  @Test
  public void testCreate() {
    String someUrl = "http://someUrl";
    String someToken = "someToken";

    ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder().withPortalUrl(someUrl).withToken(someToken).build();

    assertEquals(someUrl, client.getPortalUrl());
    assertEquals(someToken, client.getToken());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithInvalidUrl() {
    String someInvalidUrl = "someInvalidUrl";
    String someToken = "someToken";

    ApolloOpenApiClient.newBuilder().withPortalUrl(someInvalidUrl).withToken(someToken).build();
  }
}
