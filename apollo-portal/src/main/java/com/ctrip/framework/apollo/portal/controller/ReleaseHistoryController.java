package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ReleaseHistoryService;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布历史 Controller层
 */
@RestController
public class ReleaseHistoryController {

  private final ReleaseHistoryService releaseHistoryService;
  private final PermissionValidator permissionValidator;

  public ReleaseHistoryController(final ReleaseHistoryService releaseHistoryService,
      final PermissionValidator permissionValidator) {
    this.releaseHistoryService = releaseHistoryService;
    this.permissionValidator = permissionValidator;
  }

  /**
   * 通过名称空间获取发布历史列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 发布历史列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories")
  public List<ReleaseHistoryBO> findReleaseHistoriesByNamespace(@PathVariable String appId,
      @PathVariable String env,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    // 对当前用户隐藏
    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }
    // 发布历史列表
    return releaseHistoryService.findNamespaceReleaseHistory(appId, Env.valueOf(env), clusterName,
        namespaceName, page, size);
  }

}
