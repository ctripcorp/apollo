package com.ctrip.apollo.portal.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ctrip.apollo.portal.AbstractPortalTest;
import com.ctrip.apollo.portal.entity.App;
import com.ctrip.apollo.portal.entity.Privilege;

public class PrivilegeServiceTest extends AbstractPortalTest {

  @Autowired
  AppService appService;

  @Autowired
  PrivilegeService privilService;

  @Test
  public void testAddAndRemovePrivilege() {
    App newApp = new App();
    newApp.setAppId((System.currentTimeMillis()));
    newApp.setName("new app " + System.currentTimeMillis());
    newApp.setOwner("owner " + System.currentTimeMillis());
    appService.save(newApp);

    privilService.addPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.EDIT);
    List<Privilege> privileges = privilService.listPrivileges(newApp.getAppId());
    Assert.assertEquals(1, privileges.size());
    Assert.assertEquals(PrivilegeService.PrivilType.EDIT.name(), privileges.get(0).getPrivilType());
    Assert.assertEquals(newApp.getOwner(), privileges.get(0).getName());

    privilService.removePrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.EDIT);
    privileges = privilService.listPrivileges(newApp.getAppId());
    Assert.assertEquals(0, privileges.size());
  }

  @Test
  public void testCheckPrivilege() {
    App newApp = new App();
    newApp.setAppId((System.currentTimeMillis()));
    newApp.setName("new app " + System.currentTimeMillis());
    newApp.setOwner("owner " + System.currentTimeMillis());
    appService.save(newApp);

    privilService.addPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.EDIT);
    Assert.assertTrue(privilService.hasPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.EDIT));
    Assert.assertFalse(privilService.hasPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.REVIEW));
    Assert.assertFalse(privilService.hasPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.RELEASE));

    privilService.addPrivilege(newApp.getAppId(), "nobody", PrivilegeService.PrivilType.EDIT);
    Assert.assertTrue(
        privilService.hasPrivilege(newApp.getAppId(), "nobody", PrivilegeService.PrivilType.EDIT));
    Assert.assertTrue(privilService.hasPrivilege(newApp.getAppId(), newApp.getOwner(),
        PrivilegeService.PrivilType.EDIT));

    privilService.addPrivilege(newApp.getAppId(), "nobody", PrivilegeService.PrivilType.RELEASE);
    Assert.assertTrue(privilService.hasPrivilege(newApp.getAppId(), "nobody",
        PrivilegeService.PrivilType.RELEASE));
  }
}
