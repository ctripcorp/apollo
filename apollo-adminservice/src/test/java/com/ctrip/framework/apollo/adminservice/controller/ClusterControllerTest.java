package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.ConfigConsts;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ClusterControllerTest {
  private ClusterController clusterController;

  @Mock
  private ClusterService clusterService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    clusterController = new ClusterController(clusterService);
  }


  @Test(expected = BadRequestException.class)
  public void testDeleteDefaultFail() {
    Cluster cluster = new Cluster();
    cluster.setName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    when(clusterService.findOne(any(String.class), any(String.class))).thenReturn(cluster);
    clusterController.delete("1", "2", "d");
  }

  @Test
  public void testDeleteSuccess() {
    Cluster cluster = new Cluster();
    when(clusterService.findOne(any(String.class), any(String.class))).thenReturn(cluster);
    clusterController.delete("1", "2", "d");
    verify(clusterService, times(1)).findOne("1", "2");
  }
}
