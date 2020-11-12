package com.ctrip.framework.apollo.biz.repository;


import com.ctrip.framework.apollo.biz.entity.Cluster;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 集群信息 Repository层
 */
public interface ClusterRepository extends PagingAndSortingRepository<Cluster, Long> {

  /**
   * 找到指定应用id指定父集群id的集群信息
   *
   * @param appId           应用id
   * @param parentClusterId 父集群的id
   * @return 指定应用id指定父集群id的集群信息
   */
  List<Cluster> findByAppIdAndParentClusterId(String appId, Long parentClusterId);

  /**
   * 查询指定应用id的集群信息列表
   *
   * @param appId 应用id
   * @return 指定应用id的集群信息列表
   */
  List<Cluster> findByAppId(String appId);

  /**
   * 查询指定应用id指定集群名称的集群信息
   *
   * @param appId 应用id
   * @param name  集群的名称
   * @return 指定应用id指定集群名称的集群信息
   */
  Cluster findByAppIdAndName(String appId, String name);

  /**
   * 通过父集群id找到集群信息列表
   *
   * @param parentClusterId 父集群id
   * @return 指定父集群id的集群信息列表
   */
  List<Cluster> findByParentClusterId(Long parentClusterId);
}
