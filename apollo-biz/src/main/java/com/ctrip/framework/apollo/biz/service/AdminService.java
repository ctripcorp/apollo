package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

  @Autowired
  private AppService appService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private ClusterService clusterService;
  @Autowired
  private NamespaceService namespaceService;

  @Transactional
  public App createNewApp(App app) {
    String createBy = app.getDataChangeCreatedBy();
    App createdApp = appService.save(app);

    String appId = createdApp.getAppId();

    appNamespaceService.createDefaultAppNamespace(appId, createBy);

    clusterService.createDefaultCluster(appId, createBy);

    namespaceService.createPrivateNamespace(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, createBy);

    return app;
  }


}
