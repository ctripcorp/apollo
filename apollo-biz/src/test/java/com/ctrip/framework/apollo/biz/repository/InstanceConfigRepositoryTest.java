package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.AbstractIntegrationTest;
import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import java.util.Date;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by kezhenxu94 at 2019/1/18 15:33.
 *
 * @author kezhenxu94 (kezhenxu94 at 163 dot com)
 */
public class InstanceConfigRepositoryTest extends AbstractIntegrationTest {
  @Autowired
  private InstanceConfigRepository instanceConfigRepository;
  @Autowired
  private InstanceRepository instanceRepository;

  @Test
  public void shouldPaginated() {
    for (int i = 0; i < 25; i++) {
      Instance instance = new Instance();
      instance.setAppId("appId");
      instanceRepository.save(instance);

      final InstanceConfig instanceConfig = new InstanceConfig();
      instanceConfig.setConfigAppId("appId");
      instanceConfig.setInstanceId(instance.getId());
      instanceConfig.setConfigClusterName("cluster");
      instanceConfig.setConfigNamespaceName("namespace");
      instanceConfigRepository.save(instanceConfig);
    }
    Page<Object> ids = instanceConfigRepository.findInstanceIdsByNamespaceAndInstanceAppId(
        "appId", "appId", "cluster", "namespace", new Date(0), PageRequest.of(0, 10)
    );
    assertThat(ids.getContent(), hasSize(10));

    ids = instanceConfigRepository.findInstanceIdsByNamespaceAndInstanceAppId(
        "appId", "appId", "cluster", "namespace", new Date(0), PageRequest.of(1, 10)
    );
    assertThat(ids.getContent(), hasSize(10));

    ids = instanceConfigRepository.findInstanceIdsByNamespaceAndInstanceAppId(
        "appId", "appId", "cluster", "namespace", new Date(0), PageRequest.of(2, 10)
    );
    assertThat(ids.getContent(), hasSize(5));
  }
}
