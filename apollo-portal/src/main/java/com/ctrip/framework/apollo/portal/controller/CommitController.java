package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.CommitService;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提交信息 Controller
 */
@Validated
@RestController
public class CommitController {

  private final CommitService commitService;
  private final PermissionValidator permissionValidator;

  public CommitController(final CommitService commitService,
      final PermissionValidator permissionValidator) {
    this.commitService = commitService;
    this.permissionValidator = permissionValidator;
  }

  /**
   * 获取提交信息列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 提交信息列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits")
  public List<CommitDTO> find(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @Valid @PositiveOrZero(message = "page should be positive or 0") @RequestParam(defaultValue = "0") int page,
      @Valid @Positive(message = "size should be positive number") @RequestParam(defaultValue = "10") int size) {

    // 对当前用户隐藏配置
    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

    // 查询提交列表信息
    return commitService.find(appId, Env.valueOf(env), clusterName, namespaceName, page, size);
  }
}
