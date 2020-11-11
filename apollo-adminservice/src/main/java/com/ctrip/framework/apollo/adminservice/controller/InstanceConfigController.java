package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.InstanceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.common.dto.InstanceConfigDTO;
import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置的应用实例 Controller层
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/instances")
public class InstanceConfigController {

  private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();
  private final ReleaseService releaseService;
  private final InstanceService instanceService;

  public InstanceConfigController(final ReleaseService releaseService,
      final InstanceService instanceService) {
    this.releaseService = releaseService;
    this.instanceService = instanceService;
  }

  /**
   * 获取发布的实例信息
   *
   * @param releaseId 发布id
   * @param pageable  分页对象
   * @return 实例分页列表信息
   */
  @GetMapping("/by-release")
  public PageDTO<InstanceDTO> getByRelease(@RequestParam("releaseId") long releaseId,
      Pageable pageable) {
    Release release = releaseService.findOne(releaseId);
    if (release == null) {
      throw new NotFoundException(String.format("release not found for %s", releaseId));
    }

    // 最后修改时间大于验证的日期的应用实例的配置的分页信息
    Page<InstanceConfig> instanceConfigsPage = instanceService.findActiveInstanceConfigsByReleaseKey
        (release.getReleaseKey(), pageable);

    List<InstanceDTO> instanceDTOs = Collections.emptyList();

    if (instanceConfigsPage.hasContent()) {
      // 实例配置Map
      Multimap<Long, InstanceConfig> instanceConfigMap = HashMultimap.create();
      // 实例配置的发布Key
      Set<String> otherReleaseKeys = Sets.newHashSet();

      for (InstanceConfig instanceConfig : instanceConfigsPage.getContent()) {
        instanceConfigMap.put(instanceConfig.getInstanceId(), instanceConfig);
        otherReleaseKeys.add(instanceConfig.getReleaseKey());
      }

      Set<Long> instanceIds = instanceConfigMap.keySet();

      // 查询实例列表信息
      List<Instance> instances = instanceService.findInstancesByIds(instanceIds);

      if (!CollectionUtils.isEmpty(instances)) {
        instanceDTOs = BeanUtils.batchTransform(InstanceDTO.class, instances);
      }

      // 设置实例配置信息
      for (InstanceDTO instanceDTO : instanceDTOs) {
        Collection<InstanceConfig> configs = instanceConfigMap.get(instanceDTO.getId());
        List<InstanceConfigDTO> configDTOs = configs.stream().map(instanceConfig -> {
          InstanceConfigDTO instanceConfigDTO = new InstanceConfigDTO();
          //to save some space
          instanceConfigDTO.setRelease(null);
          instanceConfigDTO.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
          instanceConfigDTO.setDataChangeLastModifiedTime(instanceConfig
              .getDataChangeLastModifiedTime());
          return instanceConfigDTO;
        }).collect(Collectors.toList());
        instanceDTO.setConfigs(configDTOs);
      }
    }
    // 手动分页
    return new PageDTO<>(instanceDTOs, pageable, instanceConfigsPage.getTotalElements());
  }

  /**
   * 查询不为指定发布key集合的实例列表信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param releaseIds    发布id集
   * @return 实例列表信息
   */
  @GetMapping("/by-namespace-and-releases-not-in")
  public List<InstanceDTO> getByReleasesNotIn(@RequestParam("appId") String appId,
      @RequestParam("clusterName") String clusterName,
      @RequestParam("namespaceName") String namespaceName,
      @RequestParam("releaseIds") String releaseIds) {
    // 发布的key列表
    Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
        .collect(Collectors.toSet());

    // 发布列表信息
    List<Release> releases = releaseService.findByReleaseIds(releaseIdSet);

    if (CollectionUtils.isEmpty(releases)) {
      throw new NotFoundException(String.format("releases not found for %s", releaseIds));
    }

    Set<String> releaseKeys = releases.stream().map(Release::getReleaseKey).collect(Collectors
        .toSet());

    // 实例配置信息
    List<InstanceConfig> instanceConfigs = instanceService
        .findInstanceConfigsByNamespaceWithReleaseKeysNotIn(appId, clusterName, namespaceName,
            releaseKeys);

    // 实例配置Map集合
    Multimap<Long, InstanceConfig> instanceConfigMap = HashMultimap.create();
    // 其它发布Key的列表信息
    Set<String> otherReleaseKeys = Sets.newHashSet();

    for (InstanceConfig instanceConfig : instanceConfigs) {
      instanceConfigMap.put(instanceConfig.getInstanceId(), instanceConfig);
      otherReleaseKeys.add(instanceConfig.getReleaseKey());
    }

    // 实例信息
    List<Instance> instances = instanceService.findInstancesByIds(instanceConfigMap.keySet());

    if (CollectionUtils.isEmpty(instances)) {
      return Collections.emptyList();
    }

    List<InstanceDTO> instanceDTOs = BeanUtils.batchTransform(InstanceDTO.class, instances);

    // 其它的发布信息
    List<Release> otherReleases = releaseService.findByReleaseKeys(otherReleaseKeys);
    // 发布信息Map
    Map<String, ReleaseDTO> releaseMap = Maps.newHashMap();

    for (Release release : otherReleases) {
      //unset configurations to save space
      release.setConfigurations(null);
      ReleaseDTO releaseDTO = BeanUtils.transform(ReleaseDTO.class, release);
      releaseMap.put(release.getReleaseKey(), releaseDTO);
    }

    // 设置实例配置信息
    for (InstanceDTO instanceDTO : instanceDTOs) {
      Collection<InstanceConfig> configs = instanceConfigMap.get(instanceDTO.getId());
      List<InstanceConfigDTO> configDTOs = configs.stream().map(instanceConfig -> {
        InstanceConfigDTO instanceConfigDTO = new InstanceConfigDTO();
        instanceConfigDTO.setRelease(releaseMap.get(instanceConfig.getReleaseKey()));
        instanceConfigDTO.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
        instanceConfigDTO.setDataChangeLastModifiedTime(instanceConfig
            .getDataChangeLastModifiedTime());
        return instanceConfigDTO;
      }).collect(Collectors.toList());
      instanceDTO.setConfigs(configDTOs);
    }

    return instanceDTOs;
  }

  /**
   * 通过名称空间获取实例分页列表信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param instanceAppId 实例的应用id
   * @param pageable      分页对象
   * @return 实例分页列表信息
   */
  @GetMapping("/by-namespace")
  public PageDTO<InstanceDTO> getInstancesByNamespace(
      @RequestParam("appId") String appId, @RequestParam("clusterName") String clusterName,
      @RequestParam("namespaceName") String namespaceName,
      @RequestParam(value = "instanceAppId", required = false) String instanceAppId,
      Pageable pageable) {
    Page<Instance> instances;
    // 根据实例应用id来查询
    if (Strings.isNullOrEmpty(instanceAppId)) {
      instances = instanceService.findInstancesByNamespace(appId, clusterName,
          namespaceName, pageable);
    } else {
      instances = instanceService.findInstancesByNamespaceAndInstanceAppId(instanceAppId, appId,
          clusterName, namespaceName, pageable);
    }

    // 转换并手动分页
    List<InstanceDTO> instanceDTOs = BeanUtils.batchTransform(InstanceDTO.class,
        instances.getContent());
    return new PageDTO<>(instanceDTOs, pageable, instances.getTotalElements());
  }

  /**
   * 通过名称空间获取实例数量
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的实例数量
   */
  @GetMapping("/by-namespace/count")
  public long getInstancesCountByNamespace(@RequestParam("appId") String appId,
      @RequestParam("clusterName") String clusterName,
      @RequestParam("namespaceName") String namespaceName) {
    Page<Instance> instances = instanceService.findInstancesByNamespace(appId, clusterName,
        namespaceName, PageRequest.of(0, 1));
    return instances.getTotalElements();
  }
}
