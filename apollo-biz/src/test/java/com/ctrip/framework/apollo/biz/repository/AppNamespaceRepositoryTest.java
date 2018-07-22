package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.AbstractIntegrationTest;
import com.ctrip.framework.apollo.common.entity.AppNamespace;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppNamespaceRepositoryTest extends AbstractIntegrationTest{

  @Autowired
  private AppNamespaceRepository repository;

  @Test
  public void testFindByNameAndIsPublicTrue() throws Exception {
    AppNamespace appNamespace = repository.findByNameAndIsPublicTrue("fx.apollo.config");

    assertEquals("100003171", appNamespace.getAppId());
  }

  @Test
  public void testFindByNameAndNoPublicNamespace() throws Exception {
    AppNamespace appNamespace = repository.findByNameAndIsPublicTrue("application");

    assertNull(appNamespace);
  }

  @Test
  public void testDeleteAppNamespaceWhenDeleteApp() throws Exception{
    String appId = "100003171";
    int count = repository.countByAppId(appId);
    assertEquals(count,repository.batchDeleteByDeleteApp(appId,null));
  }
}
