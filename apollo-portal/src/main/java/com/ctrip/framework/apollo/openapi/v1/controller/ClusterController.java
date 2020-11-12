package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenClusterDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ClusterService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放API - 集群 Controller
 */
@RestController("openapiClusterController")
@RequestMapping("/openapi/v1/envs/{env}")
public class ClusterController {

  private final ClusterService clusterService;
  private final UserService userService;

  public ClusterController(final ClusterService clusterService, final UserService userService) {
    this.clusterService = clusterService;
    this.userService = userService;
  }

  /**
   * 加载集群
   *
   * @param appId       应用id
   * @param env         环境
   * @param clusterName 集群名称
   * @return 集群Dto信息
   */
  @GetMapping(value = "apps/{appId}/clusters/{clusterName:.+}")
  public OpenClusterDTO loadCluster(@PathVariable("appId") String appId, @PathVariable String env,
      @PathVariable("clusterName") String clusterName) {
    // 获取集群信息
    ClusterDTO clusterDTO = clusterService.loadCluster(appId, Env.valueOf(env), clusterName);
    return clusterDTO == null ? null : OpenApiBeanUtils.transformFromClusterDTO(clusterDTO);
  }

  /**
   * 创建集群
   *
   * @param appId   应用id
   * @param env     环境
   * @param cluster 集群
   * @param request 请求对象
   * @return 开放Api集群的Dto信息
   */
  @PreAuthorize(value = "@consumerPermissionValidator.hasCreateClusterPermission(#request, #appId)")
  @PostMapping(value = "apps/{appId}/clusters")
  public OpenClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
      @Valid @RequestBody OpenClusterDTO cluster, HttpServletRequest request) {

    if (!Objects.equals(appId, cluster.getAppId())) {
      throw new BadRequestException(String.format(
          "AppId not equal. AppId in path = %s, AppId in payload = %s", appId, cluster.getAppId()));
    }

    String clusterName = cluster.getName();
    String operator = cluster.getDataChangeCreatedBy();

    // 校验集群名称和操作人
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(clusterName, operator),
        "name and dataChangeCreatedBy should not be null or empty");

    // 不为集群名称空间，抛出
    if (!InputValidator.isValidClusterNamespace(clusterName)) {
      throw new BadRequestException(String.format("Invalid ClusterName format: %s",
          InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }

    // 用户不存在
    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("User " + operator + " doesn't exist!");
    }

    // 创建集群
    ClusterDTO toCreate = OpenApiBeanUtils.transformToClusterDTO(cluster);
    ClusterDTO createdClusterDTO = clusterService.createCluster(Env.valueOf(env), toCreate);

    return OpenApiBeanUtils.transformFromClusterDTO(createdClusterDTO);
  }

}
