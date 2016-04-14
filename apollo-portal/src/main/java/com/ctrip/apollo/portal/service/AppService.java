package com.ctrip.apollo.portal.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.ClusterNavTree;
import com.ctrip.apollo.portal.entity.SimpleMsg;


@Service
public class AppService {

  private Logger logger = LoggerFactory.getLogger(AppService.class);

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private PortalSettings portalSettings;

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;

  public ClusterNavTree buildClusterNavTree(String appId) {
    ClusterNavTree tree = new ClusterNavTree();

    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      ClusterNavTree.Node clusterNode = new ClusterNavTree.Node(env);
      clusterNode.setClusters(clusterService.findClusters(env, appId));
      tree.addNode(clusterNode);
    }
    return tree;
  }

  public boolean save(AppDTO app) {
    String createBy = app.getOwnerName();
    try {
      app.setDataChangeCreatedBy(createBy);
      app.setDataChangeCreatedTime(new Date());
      app.setDataChangeLastModifiedBy(createBy);
      appAPI.save(Env.LOCAL, app);
    } catch (Exception e) {
      logger.error("oops! save app error. app id:{}", app.getAppId(), e);
      return false;
    }
    return true;
  }


}
