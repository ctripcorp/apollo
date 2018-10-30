package com.ctrip.framework.apollo.portal.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class RoleUtilsTest {

  @Test
  public void testExtractAppIdFromMasterRoleName() throws Exception {
    assertEquals("someApp", RoleUtils.extractAppIdFromMasterRoleName("Master+someApp"));
    assertEquals("someApp", RoleUtils.extractAppIdFromMasterRoleName("Master+someApp+xx"));


    assertNull(RoleUtils.extractAppIdFromMasterRoleName("ReleaseNamespace+app1+application"));
  }
}
