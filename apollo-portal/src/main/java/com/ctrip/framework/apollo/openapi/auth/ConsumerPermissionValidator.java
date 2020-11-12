package com.ctrip.framework.apollo.openapi.auth;

import com.ctrip.framework.apollo.openapi.service.ConsumerRolePermissionService;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 消费者权限校验器
 */
@Component
public class ConsumerPermissionValidator {

  private final ConsumerRolePermissionService permissionService;
  private final ConsumerAuthUtil consumerAuthUtil;

  public ConsumerPermissionValidator(final ConsumerRolePermissionService permissionService,
      final ConsumerAuthUtil consumerAuthUtil) {
    this.permissionService = permissionService;
    this.consumerAuthUtil = consumerAuthUtil;
  }

  /**
   * 是否有修改名称空间的权限
   *
   * @param request       请求实体
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return true, 有权限，否则 ，false
   */
  public boolean hasModifyNamespacePermission(HttpServletRequest request, String appId,
      String namespaceName, String env) {
    if (hasCreateNamespacePermission(request, appId)) {
      return true;
    }
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.MODIFY_NAMESPACE, RoleUtils.buildNamespaceTargetId(appId, namespaceName))
        || permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.MODIFY_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));

  }

  /**
   * 是否有发布名称空间的权限
   *
   * @param request       请求实体
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return true, 有权限，否则 ，false
   */
  public boolean hasReleaseNamespacePermission(HttpServletRequest request, String appId,
      String namespaceName, String env) {
    if (hasCreateNamespacePermission(request, appId)) {
      return true;
    }
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.RELEASE_NAMESPACE, RoleUtils.buildNamespaceTargetId(appId, namespaceName))
        || permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.RELEASE_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, env));

  }

  /**
   * 是否有创建名称空间的权限
   *
   * @param request 请求实体
   * @param appId   应用id
   * @return true, 有权限，否则 ，false
   */

  public boolean hasCreateNamespacePermission(HttpServletRequest request, String appId) {
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.CREATE_NAMESPACE, appId);
  }

  /**
   * 是否有创建集群的权限
   *
   * @param request 请求实体
   * @param appId 应用id
   * @return true, 有权限，否则 ，false
   */
  public boolean hasCreateClusterPermission(HttpServletRequest request, String appId) {
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.CREATE_CLUSTER, appId);
  }
}
