package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.environment.Env;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 实例服务
 */
@Service
public class InstanceService {


  private final AdminServiceAPI.InstanceAPI instanceAPI;

  public InstanceService(final AdminServiceAPI.InstanceAPI instanceAPI) {
    this.instanceAPI = instanceAPI;
  }

  /**
   * 获取指定环境的指定发布信息的实例分页信息
   *
   * @param env       环境
   * @param releaseId 发布id
   * @param page      页码
   * @param size      页面大小
   * @return 指定环境的指定发布信息的实例分页信息
   */
  public PageDTO<InstanceDTO> getByRelease(Env env, long releaseId, int page, int size) {
    return instanceAPI.getByRelease(env, releaseId, page, size);
  }

  /**
   * 获取指定环境的指定名称空间实例分页信息
   *
   * @param appId         发布id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param instanceAppId 实例应用id
   * @param page          页码
   * @param size          页面大小
   * @return 指定环境的指定名称空间实例分页信息
   */
  public PageDTO<InstanceDTO> getByNamespace(Env env, String appId, String clusterName,
      String namespaceName, String instanceAppId, int page, int size) {
    return instanceAPI.getByNamespace(appId, env, clusterName, namespaceName, instanceAppId, page,
        size);
  }

  /**
   * 通过名称空间获取实例数量
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的实例数量
   */
  public int getInstanceCountByNamepsace(String appId, Env env, String clusterName,
      String namespaceName) {
    return instanceAPI.getInstanceCountByNamespace(appId, env, clusterName, namespaceName);
  }

  /**
   * 查询不为指定发布key集合的实例列表信息
   *
   * @param env           环境
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param releaseIds    发布id集
   * @return 实例列表信息
   */
  public List<InstanceDTO> getByReleasesNotIn(Env env, String appId, String clusterName,
      String namespaceName, Set<Long> releaseIds) {
    return instanceAPI.getByReleasesNotIn(appId, env, clusterName, namespaceName, releaseIds);
  }


}
