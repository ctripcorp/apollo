package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.Instance;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 使用配置的应用实例 Repository层
 */
public interface InstanceRepository extends PagingAndSortingRepository<Instance, Long> {

  /**
   * 通过应用id、集群名称、数据中心、实例ip地址查询使用配置的应用实例
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param dataCenter  数据中心
   * @param ip          实例ip地址
   * @return 符合条件的使用配置的应用实例
   */
  Instance findByAppIdAndClusterNameAndDataCenterAndIp(String appId, String clusterName,
      String dataCenter, String ip);
}
