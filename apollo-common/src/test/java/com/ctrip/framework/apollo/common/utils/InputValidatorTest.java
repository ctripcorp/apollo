package com.ctrip.framework.apollo.common.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class InputValidatorTest {

  @Test
  public void testValidClusterName() throws Exception {
    checkClusterName("some.cluster-_name.123", true);
    checkClusterName("some.cluster-_name.123.yml", true);
    checkClusterName("some.&.name", false);
    checkClusterName("", false);
    checkClusterName(null, false);
  }

  @Test
  public void testValidAppNamespaceName() throws Exception {
    checkAppNamespaceName("some.cluster-_name.123", true);
    checkAppNamespaceName("some.&.name", false);
    checkAppNamespaceName("", false);
    checkAppNamespaceName(null, false);
    checkAppNamespaceName("some.name.json", false);
    checkAppNamespaceName("some.name.yml", false);
    checkAppNamespaceName("some.name.yaml", false);
    checkAppNamespaceName("some.name.xml", false);
    checkAppNamespaceName("some.name.properties", false);
  }

  private void checkClusterName(String name, boolean valid) {
    assertEquals(valid, InputValidator.isValidClusterNamespace(name));
  }

  private void checkAppNamespaceName(String name, boolean valid) {
    assertEquals(valid, InputValidator.isValidAppNamespace(name));
  }
}
