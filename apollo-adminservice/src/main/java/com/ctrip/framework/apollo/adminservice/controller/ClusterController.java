package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群 Controller层
 */
@RestController
public class ClusterController {

  private final ClusterService clusterService;

  public ClusterController(final ClusterService clusterService) {
    this.clusterService = clusterService;
  }

  /**
   * 创建集群
   *
   * @param appId                      应用名称
   * @param autoCreatePrivateNamespace 是否自动创建私有的名称空间
   * @param dto                        集群信息
   * @return 创建后的集群信息
   */
  @PostMapping("/apps/{appId}/clusters")
  public ClusterDTO create(@PathVariable("appId") String appId,
      @RequestParam(value = "autoCreatePrivateNamespace", defaultValue = "true") boolean autoCreatePrivateNamespace,
      @Valid @RequestBody ClusterDTO dto) {

    Cluster entity = BeanUtils.transform(Cluster.class, dto);
    // 判断是否已经存在
    Cluster managedEntity = clusterService.findOne(appId, entity.getName());
    if (managedEntity != null) {
      throw new BadRequestException("cluster already exist.");
    }

    if (autoCreatePrivateNamespace) {
      // 保存应用名称空间的集群实例
      entity = clusterService.saveWithInstanceOfAppNamespaces(entity);
    } else {
      // 保存应用名称空间没有实例的集群信息
      entity = clusterService.saveWithoutInstanceOfAppNamespaces(entity);
    }

    return BeanUtils.transform(ClusterDTO.class, entity);
  }

  /**
   * 删除集群信息
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @param operator    操作者
   */
  @DeleteMapping("/apps/{appId}/clusters/{clusterName:.+}")
  public void delete(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName, @RequestParam String operator) {

    // 判断集群是否存在
    Cluster entity = clusterService.findOne(appId, clusterName);
    if (entity == null) {
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    }

    //  默认的集群不能删除
    if (ConfigConsts.CLUSTER_NAME_DEFAULT.equals(entity.getName())) {
      throw new BadRequestException("can not delete default cluster!");
    }

    clusterService.delete(entity.getId(), operator);
  }

  /**
   * 查询应用下的集群列表
   *
   * @param appId 应用id
   * @return 集群列表
   */
  @GetMapping("/apps/{appId}/clusters")
  public List<ClusterDTO> find(@PathVariable("appId") String appId) {
    List<Cluster> clusters = clusterService.findParentClusters(appId);
    return BeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  /**
   * 通过应用id和名称查询集群信息
   *
   * @param appId       应用id
   * @param clusterName 集群名称
   * @return 集群信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName:.+}")
  public ClusterDTO get(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    Cluster cluster = clusterService.findOne(appId, clusterName);
    if (cluster == null) {
      throw new NotFoundException("cluster not found for name " + clusterName);
    }
    return BeanUtils.transform(ClusterDTO.class, cluster);
  }

  /**
   * 集群名称是否唯一
   *
   * @param appId       应用id
   * @param clusterName 名称空间名称
   * @return true, 唯一，否则，false,名称已经存在
   */
  @GetMapping("/apps/{appId}/cluster/{clusterName}/unique")
  public boolean isAppIdUnique(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    return clusterService.isClusterNameUnique(appId, clusterName);
  }
}
