package com.ctrip.framework.apollo.openapi.auth;

import com.ctrip.framework.apollo.openapi.service.ConsumerRolePermissionService;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class ConsumerPermissionValidator {

  @Autowired
  private ConsumerRolePermissionService permissionService;
  @Autowired
  private ConsumerAuthUtil consumerAuthUtil;


  public boolean hasModifyNamespacePermission(HttpServletRequest request, String appId, String
      namespaceName) {

    if (hasCreateNamespacePermission(request, appId)) {
      return true;
    }
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.MODIFY_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, null));

  }

  public boolean hasReleaseNamespacePermission(HttpServletRequest request, String appId, String
      namespaceName) {
    if (hasCreateNamespacePermission(request, appId)) {
      return true;
    }
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
        PermissionType.RELEASE_NAMESPACE,
        RoleUtils.buildNamespaceTargetId(appId, namespaceName, null));

  }

  public boolean hasCreateNamespacePermission(HttpServletRequest request, String appId) {
    return permissionService.consumerHasPermission(consumerAuthUtil.retrieveConsumerId(request),
                                                   PermissionType.CREATE_NAMESPACE,
                                                   appId);
  }

}
