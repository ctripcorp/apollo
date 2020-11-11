package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 集群 Service
 */
@Service
public class ClusterService {

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.ClusterAPI clusterAPI;

  public ClusterService(final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.ClusterAPI clusterAPI) {
    this.userInfoHolder = userInfoHolder;
    this.clusterAPI = clusterAPI;
  }

  /**
   * 查询指定环境指定应用的集群列表信息
   *
   * @param env   指定环境
   * @param appId 指定应用id
   * @return 集群列表信息
   */
  public List<ClusterDTO> findClusters(Env env, String appId) {
    return clusterAPI.findClustersByApp(appId, env);
  }

  /**
   * 创建集群
   *
   * @param env     环境
   * @param cluster 待创建的集群信息
   * @return 创建后的集群信息
   */
  public ClusterDTO createCluster(Env env, ClusterDTO cluster) {
    if (!clusterAPI.isClusterUnique(cluster.getAppId(), env, cluster.getName())) {
      throw new BadRequestException(String.format("cluster %s already exists.", cluster.getName()));
    }
    // 创建
    ClusterDTO clusterDTO = clusterAPI.create(env, cluster);
    Tracer.logEvent(TracerEventType.CREATE_CLUSTER, cluster.getAppId(), "0", cluster.getName());
    return clusterDTO;
  }

  /**
   * 删除集群信息
   *
   * @param env         环境
   * @param appId       应用id
   * @param clusterName 集群名称
   */
  public void deleteCluster(Env env, String appId, String clusterName) {
    clusterAPI.delete(env, appId, clusterName, userInfoHolder.getUser().getUserId());
  }

  /**
   * 加载指定的集群信息
   *
   * @param env         环境
   * @param appId       应用id
   * @param clusterName 集群名称
   * @return 指定的集群信息
   */
  public ClusterDTO loadCluster(String appId, Env env, String clusterName) {
    return clusterAPI.loadCluster(appId, env, clusterName);
  }

}
