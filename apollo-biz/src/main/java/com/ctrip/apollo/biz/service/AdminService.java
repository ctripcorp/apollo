package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.apollo.biz.repository.AppRepository;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;

import java.util.Date;

@Service
public class AdminService {
  private static final String DEFAULT_NAMESPACE_NAME = "application";
  private static final String DEFAULT_CLUSTER_NAME = "default";

  @Autowired
  private AppRepository appRepository;

  @Autowired
  private AppNamespaceRepository appNamespaceRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private ClusterRepository clusterRepository;

  public App createNewApp(App app) {
    String createBy = app.getDataChangeCreatedBy();

    App createdApp = appRepository.save(app);
    String appId = createdApp.getAppId();

    createDefaultAppNamespace(appId, createBy);

    createDefaultCluster(appId, createBy);

    createDefaultNamespace(appId, createBy);

    return app;
  }

  private void createDefaultAppNamespace(String appId, String createBy){
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(DEFAULT_NAMESPACE_NAME);
    appNs.setComment("default app namespace");
    appNs.setDataChangeCreatedBy(createBy);
    appNs.setDataChangeCreatedTime(new Date());
    appNs.setDataChangeLastModifiedBy(createBy);
    appNamespaceRepository.save(appNs);
  }

  private void createDefaultCluster(String appId, String createBy){
    Cluster cluster = new Cluster();
    cluster.setName(DEFAULT_CLUSTER_NAME);
    cluster.setAppId(appId);
    cluster.setDataChangeCreatedBy(createBy);
    cluster.setDataChangeCreatedTime(new Date());
    cluster.setDataChangeLastModifiedBy(createBy);
    clusterRepository.save(cluster);
  }

  private void createDefaultNamespace(String appId, String createBy){
    Namespace ns = new Namespace();
    ns.setAppId(appId);
    ns.setClusterName(DEFAULT_CLUSTER_NAME);
    ns.setNamespaceName(DEFAULT_NAMESPACE_NAME);
    ns.setDataChangeCreatedBy(createBy);
    ns.setDataChangeCreatedTime(new Date());
    ns.setDataChangeLastModifiedBy(createBy);
    namespaceRepository.save(ns);
  }
}
