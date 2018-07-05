package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  @Autowired
  private ReleaseHistoryService releaseHistoryService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private GrayReleaseRuleService grayReleaseRuleService;
  @Autowired
  private CommitService commitService;

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

  /**
   * 逻辑删除App，将原来appId设置为固定格式（DELETED_${appID}_timestamp），并记录操作人
   */
  @Transactional
  public void deleteApp(String oldAppId, String newAppId, String operator) {
    logger.info("{} is deleting App:{}", operator, oldAppId);

    //1、删除发布历史，ReleaseHistory
    releaseHistoryService.batchDeleteByDeleteApp(oldAppId, newAppId, operator);

    //2、删除发布
    releaseService.deleteApp(oldAppId, newAppId, operator);

    //3、删除Namespace
    namespaceService.deleteApp(oldAppId, newAppId, operator);

    //4、删除GrayReleaseRule
    grayReleaseRuleService.deleteApp(oldAppId, newAppId, operator);

    //5、删除历史表
    commitService.deleteApp(oldAppId, newAppId, operator);

    //6、删除集群
    clusterService.deleteApp(oldAppId, newAppId, operator);

    //7、删除AppNamespace
    appNamespaceService.deleteApp(oldAppId, newAppId, operator);

    //8、删除App
    appService.deleteApp(oldAppId, newAppId, operator);
  }
}
