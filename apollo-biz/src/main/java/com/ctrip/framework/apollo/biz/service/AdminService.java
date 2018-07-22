package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.ConfigConsts;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
  @Autowired
  private ReleaseHistoryService releaseHistoryService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private GrayReleaseRuleService grayReleaseRuleService;
  @Autowired
  private CommitService commitService;
  @Autowired
  private ItemService itemService;

  final static Logger logger = LoggerFactory.getLogger(AdminService.class);

  @Transactional
  public App createNewApp(App app) {
    String createBy = app.getDataChangeCreatedBy();
    App createdApp = appService.save(app);

    String appId = createdApp.getAppId();

    appNamespaceService.createDefaultAppNamespace(appId, createBy);

    clusterService.createDefaultCluster(appId, createBy);

    namespaceService.instanceOfAppNamespaces(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, createBy);

    return app;
  }

  @Transactional
  public void deleteApp(String appId, String operator) {
    logger.info("{} is deleting App:{}", operator, appId);

    List<Cluster> managedClusters = clusterService.findClusters(appId);

    Set<Namespace> managedNamespaces = Sets.newLinkedHashSet();

    if (Objects.nonNull(managedClusters)) {
      for (Cluster cluster : managedClusters) {
        managedNamespaces.addAll(namespaceService.findNamespaces(appId, cluster.getName()));
      }
    }

    //1.delete release history.
    releaseHistoryService.batchDeleteByDeleteApp(appId, operator);

    //2.delete release.
    releaseService.deleteApp(appId, operator);

    //3.delete config items.
    for (Namespace namespace : managedNamespaces) {
      itemService.batchDelete(namespace.getId(), operator);
    }

    //4.delete Namespaces
    namespaceService.deleteApp(managedNamespaces, operator);

    //5.delete GrayReleaseRule
    grayReleaseRuleService.deleteApp(appId, operator);

    //6.delete history.
    commitService.deleteApp(appId, operator);

    //7.delete cluster
    clusterService.deleteApp(appId, operator);

    //8.delete appNamespace
    appNamespaceService.deleteApp(appId, operator);

    //9.delete app
    appService.deleteApp(appId, operator);
  }
}
