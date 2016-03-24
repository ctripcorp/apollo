package com.ctrip.apollo.portal.repository;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ctrip.apollo.portal.AbstractPortalTest;
import com.ctrip.apollo.portal.entity.App;


public class AppRepositoryTest extends AbstractPortalTest{

  @Autowired
  AppRepository repository;

  @Test
  public void testCreate() {
    Assert.assertEquals(0, repository.count());

    App ramdomApp = new App();
    ramdomApp.setAppId(System.currentTimeMillis());
    ramdomApp.setName("new app " + System.currentTimeMillis());
    ramdomApp.setOwner("owner " + System.currentTimeMillis());
    repository.save(ramdomApp);

    Assert.assertEquals(1, repository.count());
  }
}
