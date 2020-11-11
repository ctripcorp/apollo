package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.repository.InstanceConfigRepository;
import com.ctrip.framework.apollo.biz.repository.InstanceRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 配置的应用实例 Service层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class InstanceService {

  private final InstanceRepository instanceRepository;
  private final InstanceConfigRepository instanceConfigRepository;

  public InstanceService(
      final InstanceRepository instanceRepository,
      final InstanceConfigRepository instanceConfigRepository) {
    this.instanceRepository = instanceRepository;
    this.instanceConfigRepository = instanceConfigRepository;
  }

  /**
   * 查询指定实例信息
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param dataCenter  数据中心
   * @param ip          实例ip地址
   * @return 实例信息
   */
  public Instance findInstance(String appId, String clusterName, String dataCenter, String ip) {
    return instanceRepository.findByAppIdAndClusterNameAndDataCenterAndIp(appId, clusterName,
        dataCenter, ip);
  }

  /**
   * 通过实例id列表查询实例列表信息
   *
   * @param instanceIds 实例id集
   * @return 实例列表信息
   */
  public List<Instance> findInstancesByIds(Set<Long> instanceIds) {
    Iterable<Instance> instances = instanceRepository.findAllById(instanceIds);
    return Lists.newArrayList(instances);
  }

  /**
   * 保存
   *
   * @param instance 实例信息
   * @return 创建的实例信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Instance createInstance(Instance instance) {
    //protection
    instance.setId(0);

    return instanceRepository.save(instance);
  }

  /**
   * 查询应用实例的配置信息
   *
   * @param instanceId          实例id
   * @param configAppId         配置的应用id
   * @param configNamespaceName 配置的名称空间名称
   * @return 应用实例的配置信息
   */
  public InstanceConfig findInstanceConfig(long instanceId, String configAppId, String
      configNamespaceName) {
    return instanceConfigRepository.findByInstanceIdAndConfigAppIdAndConfigNamespaceName(
        instanceId, configAppId, configNamespaceName);
  }

  /**
   * 通过发布的Key查询最后修改时间大于验证的日期的应用实例配置的分页信息
   *
   * @param releaseKey 发布的key
   * @param pageable   分页对象
   * @return 实例配置分页列表信息
   */
  public Page<InstanceConfig> findActiveInstanceConfigsByReleaseKey(String releaseKey, Pageable
      pageable) {
    return instanceConfigRepository.findByReleaseKeyAndDataChangeLastModifiedTimeAfter(releaseKey,
        getValidInstanceConfigDate(), pageable);
  }

  /**
   * 通过名称空间查询实例分页列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 实例分页列表
   */
  public Page<Instance> findInstancesByNamespace(String appId, String clusterName, String
      namespaceName, Pageable pageable) {
    // 通过配置的AppId、配置的集群名称、配置的名称空间名称查询最后修改时间大于验证的日期并且指定发布的Key不存在的应用实例的配置列表信息
    Page<InstanceConfig> instanceConfigs = instanceConfigRepository.
        findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfter(
            appId, clusterName, namespaceName, getValidInstanceConfigDate(), pageable);

    List<Instance> instances = Collections.emptyList();
    if (instanceConfigs.hasContent()) {
      // 过滤实例id，然后再次通过实例id查询实例列表信息
      Set<Long> instanceIds = instanceConfigs.getContent().stream().map
          (InstanceConfig::getInstanceId).collect(Collectors.toSet());
      instances = findInstancesByIds(instanceIds);
    }

    // 手动分页
    return new PageImpl<>(instances, pageable, instanceConfigs.getTotalElements());
  }

  /**
   * 查询实例分页信息
   *
   * @param instanceAppId 实例的应用id
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 实例分页列表
   */
  public Page<Instance> findInstancesByNamespaceAndInstanceAppId(String instanceAppId, String
      appId, String clusterName, String namespaceName, Pageable pageable) {
    // 通过配置的AppId、配置的集群名称、配置的名称空间名称、配置的AppId查询最后修改时间大于验证的日期的实例id分页列表
    Page<Object> instanceIdResult = instanceConfigRepository
        .findInstanceIdsByNamespaceAndInstanceAppId(instanceAppId, appId, clusterName,
            namespaceName, getValidInstanceConfigDate(), pageable);

    List<Instance> instances = Collections.emptyList();
    if (instanceIdResult.hasContent()) {
      // 取出实例id
      Set<Long> instanceIds = instanceIdResult.getContent().stream().map((Object o) -> {
        if (o == null) {
          return null;
        }

        if (o instanceof Integer) {
          return ((Integer) o).longValue();
        }

        if (o instanceof Long) {
          return (Long) o;
        }

        //for h2 test
        if (o instanceof BigInteger) {
          return ((BigInteger) o).longValue();
        }

        return null;
      }).filter(Objects::nonNull).collect(Collectors.toSet());
      // 通过实例id再次查询实例信息
      instances = findInstancesByIds(instanceIds);
    }
    // 手动分页
    return new PageImpl<>(instances, pageable, instanceIdResult.getTotalElements());
  }

  /**
   * 查询实例配置信息
   *
   * @param appId            应用id
   * @param clusterName      集群名称
   * @param namespaceName    名称空间名称
   * @param releaseKeysNotIn 排除的发布key
   * @return 实例配置列表信息
   */
  public List<InstanceConfig> findInstanceConfigsByNamespaceWithReleaseKeysNotIn(String appId,
      String clusterName, String namespaceName, Set<String> releaseKeysNotIn) {
    // 通过配置的AppId、配置的集群名称、配置的名称空间名称查询最后修改时间大于验证的日期并且指定发布的Key不存在的应用实例的配置列表信息
    List<InstanceConfig> instanceConfigs = instanceConfigRepository.
        findByConfigAppIdAndConfigClusterNameAndConfigNamespaceNameAndDataChangeLastModifiedTimeAfterAndReleaseKeyNotIn(
            appId, clusterName, namespaceName, getValidInstanceConfigDate(), releaseKeysNotIn);

    if (CollectionUtils.isEmpty(instanceConfigs)) {
      return Collections.emptyList();
    }

    return instanceConfigs;
  }

  /**
   * 获取验证实例配置的日期(当前实例配置过期1天，增加一个小时以避免可能的时差)
   */
  private Date getValidInstanceConfigDate() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    cal.add(Calendar.HOUR, -1);
    return cal.getTime();
  }

  /**
   * 创建实例配置
   *
   * @param instanceConfig 实例配置信息
   * @return 创建的实例配置信息
   */
  @Transactional(rollbackFor = Exception.class)
  public InstanceConfig createInstanceConfig(InstanceConfig instanceConfig) {
    //protection
    instanceConfig.setId(0);
    return instanceConfigRepository.save(instanceConfig);
  }

  /**
   * 更新实例配置
   *
   * @param instanceConfig 新的实例配置信息
   * @return 更新后的实例配置信息
   */
  @Transactional(rollbackFor = Exception.class)
  public InstanceConfig updateInstanceConfig(InstanceConfig instanceConfig) {
    InstanceConfig existedInstanceConfig = instanceConfigRepository.findById(instanceConfig.getId())
        .orElse(null);
    Preconditions.checkArgument(existedInstanceConfig != null, String.format(
        "Instance config %d doesn't exist", instanceConfig.getId()));

    existedInstanceConfig.setConfigClusterName(instanceConfig.getConfigClusterName());
    existedInstanceConfig.setReleaseKey(instanceConfig.getReleaseKey());
    existedInstanceConfig.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
    existedInstanceConfig.setDataChangeLastModifiedTime(instanceConfig
        .getDataChangeLastModifiedTime());
    return instanceConfigRepository.save(existedInstanceConfig);
  }

  /**
   * 批量删除实例配置
   *
   * @param configAppId         配置应用id
   * @param configClusterName   配置集群名称
   * @param configNamespaceName 配置名称空间名称
   * @return 影响的行数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchDeleteInstanceConfig(String configAppId, String configClusterName,
      String configNamespaceName) {
    return instanceConfigRepository.batchDelete(configAppId, configClusterName,
        configNamespaceName);
  }
}
