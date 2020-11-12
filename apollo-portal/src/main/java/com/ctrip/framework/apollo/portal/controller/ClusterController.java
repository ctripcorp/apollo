package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ClusterService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群 Controller
 */
@RestController
public class ClusterController {

  private final ClusterService clusterService;
  private final UserInfoHolder userInfoHolder;

  public ClusterController(final ClusterService clusterService,
      final UserInfoHolder userInfoHolder) {
    this.clusterService = clusterService;
    this.userInfoHolder = userInfoHolder;
  }

  /**
   * 创建集群
   *
   * @param appId   应用id
   * @param env     环境
   * @param cluster 集群信息
   * @return 创建的集群信息
   */
  @PreAuthorize(value = "@permissionValidator.hasCreateClusterPermission(#appId)")
  @PostMapping(value = "apps/{appId}/envs/{env}/clusters")
  public ClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
      @Valid @RequestBody ClusterDTO cluster) {
    // 设置 ClusterDTO 的创建和修改人为当前管理员
    String operator = userInfoHolder.getUser().getUserId();
    cluster.setDataChangeLastModifiedBy(operator);
    cluster.setDataChangeCreatedBy(operator);
    // 创建 Cluster 到 Admin Service
    return clusterService.createCluster(Env.valueOf(env), cluster);
  }

  /**
   * 删除集群
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return ResponseEntity.ok()
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ResponseEntity<Void> deleteCluster(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {
    clusterService.deleteCluster(Env.valueOf(env), appId, clusterName);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ClusterDTO loadCluster(@PathVariable("appId") String appId, @PathVariable String env,
      @PathVariable("clusterName") String clusterName) {

    return clusterService.loadCluster(appId, Env.valueOf(env), clusterName);
  }

}
