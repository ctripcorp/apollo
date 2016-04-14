package com.ctrip.apollo.biz.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ctrip.apollo.biz.BizTestConfiguration;
import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.entity.Privilege;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BizTestConfiguration.class)
public class PrivilegeServiceTest {

  @Autowired
  private AdminService adminService;

  @Autowired
  private ViewService viewService;

  @Autowired
  private PrivilegeService privilService;

  @Test
  public void testAddAndRemovePrivilege() {
    App app = new App();
    app.setAppId(String.valueOf(System.currentTimeMillis()));
    app.setName("new app " + System.currentTimeMillis());
    String owner = "owner " + System.currentTimeMillis();
    app.setOwnerName(owner);
    app.setOwnerEmail("owner " + System.currentTimeMillis() + "@ctrip.com");
    app.setDataChangeCreatedBy(owner);
    app.setDataChangeLastModifiedBy(owner);
    app.setDataChangeCreatedTime(new Date());
    App newApp = adminService.createNewApp(app);

    List<Cluster> clusters = viewService.findClusters(newApp.getAppId());
    List<Namespace> namespaces =
        viewService.findNamespaces(newApp.getAppId(), clusters.get(0).getName());
    Namespace namespace = namespaces.get(0);

    privilService.addPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.EDIT);
    List<Privilege> privileges = privilService.listPrivileges(namespace.getId());
    Assert.assertEquals(1, privileges.size());
    Assert.assertEquals(PrivilegeService.PrivilType.EDIT.name(), privileges.get(0).getPrivilType());
    Assert.assertEquals(newApp.getOwnerName(), privileges.get(0).getName());

    privilService.removePrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.EDIT);
    privileges = privilService.listPrivileges(namespace.getId());
    Assert.assertEquals(0, privileges.size());
  }

  @Test
  public void testCheckPrivilege() {
    App app = new App();
    app.setAppId(String.valueOf(System.currentTimeMillis()));
    app.setName("new app " + System.currentTimeMillis());
    String owner = "owner " + System.currentTimeMillis();
    app.setOwnerName(owner);
    app.setOwnerEmail("owner " + System.currentTimeMillis() + "@ctrip.com");
    app.setDataChangeCreatedBy(owner);
    app.setDataChangeLastModifiedBy(owner);
    app.setDataChangeCreatedTime(new Date());
    App newApp = adminService.createNewApp(app);
    List<Cluster> clusters = viewService.findClusters(newApp.getAppId());
    List<Namespace> namespaces =
        viewService.findNamespaces(newApp.getAppId(), clusters.get(0).getName());
    Namespace namespace = namespaces.get(0);

    privilService.addPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.EDIT);
    Assert.assertTrue(privilService.hasPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.EDIT));
    Assert.assertFalse(privilService.hasPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.REVIEW));
    Assert.assertFalse(privilService.hasPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.RELEASE));

    privilService.addPrivilege(namespace.getId(), "nobody", PrivilegeService.PrivilType.EDIT);
    Assert.assertTrue(
        privilService.hasPrivilege(namespace.getId(), "nobody", PrivilegeService.PrivilType.EDIT));
    Assert.assertTrue(privilService.hasPrivilege(namespace.getId(), newApp.getOwnerName(),
        PrivilegeService.PrivilType.EDIT));

    privilService.addPrivilege(namespace.getId(), "nobody", PrivilegeService.PrivilType.RELEASE);
    Assert.assertTrue(privilService.hasPrivilege(namespace.getId(), "nobody",
        PrivilegeService.PrivilType.RELEASE));
  }
}
