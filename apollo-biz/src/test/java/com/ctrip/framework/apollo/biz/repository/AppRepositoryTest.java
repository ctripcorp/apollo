package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.AbstractIntegrationTest;
import com.ctrip.framework.apollo.common.entity.App;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppRepositoryTest extends AbstractIntegrationTest{

  @Autowired
  private AppRepository appRepository;

  @Test
  public void testCreate() {
    String appId = "someAppId";
    String appName = "someAppName";
    String ownerName = "someOwnerName";
    String ownerEmail = "someOwnerName@ctrip.com";

    App app = new App();
    app.setAppId(appId);
    app.setName(appName);
    app.setOwnerName(ownerName);
    app.setOwnerEmail(ownerEmail);

    Assert.assertEquals(0, appRepository.count());

    appRepository.save(app);

    Assert.assertEquals(1, appRepository.count());
  }

  @Test
  public void testRemove() {
    String appId = "someAppId";
    String appName = "someAppName";
    String ownerName = "someOwnerName";
    String ownerEmail = "someOwnerName@ctrip.com";

    App app = new App();
    app.setAppId(appId);
    app.setName(appName);
    app.setOwnerName(ownerName);
    app.setOwnerEmail(ownerEmail);

    Assert.assertEquals(0, appRepository.count());

    appRepository.save(app);

    Assert.assertEquals(1, appRepository.count());

    appRepository.delete(app.getId());

    Assert.assertEquals(0, appRepository.count());
  }

  @Test
  public void testDelete() {
    String appId = "someAppId";
    String appName = "someAppName";
    String ownerName = "someOwnerName";
    String ownerEmail = "someOwnerName@ctrip.com";
    String operator = "someOwnerName";

    App app = new App();
    app.setAppId(appId);
    app.setName(appName);
    app.setOwnerName(ownerName);
    app.setOwnerEmail(ownerEmail);

    Assert.assertEquals(0, appRepository.count());

    appRepository.save(app);

    Assert.assertEquals(1, appRepository.count());

    Assert.assertEquals(1, appRepository.deleteApp(app.getAppId(), operator));

    Assert.assertEquals(0, appRepository.count());
  }

}
