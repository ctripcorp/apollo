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

  @Test
  public void testExtractAppIdFromRoleName() throws Exception {
    assertEquals("someApp", RoleUtils.extractAppIdFromRoleName("Master+someApp"));
    assertEquals("someApp", RoleUtils.extractAppIdFromRoleName("ModifyNamespace+someApp+xx"));
    assertEquals("app1", RoleUtils.extractAppIdFromRoleName("ReleaseNamespace+app1+application"));
  }
}
