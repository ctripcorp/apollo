package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 应用实例的配置信息  Repository层
 */
public interface InstanceConfigRepository extends PagingAndSortingRepository<InstanceConfig, Long> {

  /**
   * 通过实例id、配置的AppId、配置的名称空间名称查询应用实例的配置信息
   *
   * @param instanceId          实例id
   * @param configAppId         配置的应用id
   * @param configNamespaceName 配置的名称空间名称
   * @return 符合条件的应用实例的配置信息
   */
  InstanceConfig findByInstanceIdAndConfigAppIdAndConfigNamespaceName(long instanceId, String
      configAppId, String configNamespaceName);

  /**
   * 通过发布的Key查询最后修改时间大于验证的日期的应用实例的配置分页信息
   *
   * @param releaseKey 发布的Key
   * @param validDate  验证的日期
   * @param pageable   分页对象
   * @return 符合条件的应用实例的配置分页信息
   */
  Page<InstanceConfig> findByReleaseKeyAndDataChangeLastModifiedTimeAfter(String releaseKey, Date
      validDate, Pageable pageable);

  /**
   * 通过配置的AppId、配置的集群名称、配置的名称空间名称查询最后修改时间大于验证的日期的应用实例的配置分页信息
   *
   * @param appId         配置的AppId
   * @param clusterName   配置的集群名称
   * @param namespaceName 配置的名称空间名称
   * @param validDate     验证的日期
   * @param pageable      分页对象
   * @return 符合条件的应用实例的配置分页信息
   */
  Page<InstanceConfig> findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfter(
      String appId, String clusterName, String namespaceName, Date validDate, Pageable pageable);

  /**
   * 通过配置的AppId、配置的集群名称、配置的名称空间名称查询最后修改时间大于验证的日期并且指定发布的Key不存在的应用实例的配置列表信息
   *
   * @param appId         配置的AppId
   * @param clusterName   配置的集群名称
   * @param namespaceName 配置的名称空间名称
   * @param validDate     验证的日期
   * @param releaseKey    发布的Key
   * @return 符合条件的应用实例的配置列表信息
   */
  List<InstanceConfig> findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfterAndReleaseKeyNotIn(
      String appId, String clusterName, String namespaceName, Date validDate,
      Set<String> releaseKey);

  /**
   * 通过配置的AppId、配置的集群名称、配置的名称空间名称删除应用实例的配置信息
   *
   * @param appId         配置的AppId
   * @param clusterName   配置的集群名称
   * @param namespaceName 配置的名称空间名称
   * @return 影响的行数
   */
  @Modifying
  @Query("delete from InstanceConfig  where ConfigAppId=?1 and ConfigClusterName=?2 and ConfigNamespaceName = ?3")
  int batchDelete(String appId, String clusterName, String namespaceName);

  /**
   * 通过配置的AppId、配置的集群名称、配置的名称空间名称、配置的AppId查询最后修改时间大于验证的日期的实例id分页列表
   *
   * @param instanceAppId 实例id
   * @param configAppId   配置的AppId
   * @param clusterName   配置的集群名称
   * @param namespaceName 配置的名称空间名称
   * @param validDate     验证的日期
   * @param pageable      分页对象
   * @return 符合条件的分实例id分页列表
   */
  @Query(
      value = "select b.Id from `InstanceConfig` a inner join `Instance` b on b.Id =" +
          " a.`InstanceId` where a.`ConfigAppId` = :configAppId and a.`ConfigClusterName` = " +
          ":clusterName and a.`ConfigNamespaceName` = :namespaceName and a.`DataChange_LastTime` " +
          "> :validDate and b.`AppId` = :instanceAppId",
      countQuery = "select count(1) from `InstanceConfig` a inner join `Instance` b on b.id =" +
          " a.`InstanceId` where a.`ConfigAppId` = :configAppId and a.`ConfigClusterName` = " +
          ":clusterName and a.`ConfigNamespaceName` = :namespaceName and a.`DataChange_LastTime` " +
          "> :validDate and b.`AppId` = :instanceAppId",
      nativeQuery = true)
  Page<Object> findInstanceIdsByNamespaceAndInstanceAppId(
      @Param("instanceAppId") String instanceAppId, @Param("configAppId") String configAppId,
      @Param("clusterName") String clusterName, @Param("namespaceName") String namespaceName,
      @Param("validDate") Date validDate, Pageable pageable);
}
