package com.ctrip.framework.apollo.portal.controller;

import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceCreationModel;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class NamespaceController {

  Logger logger = LoggerFactory.getLogger(NamespaceController.class);

  @Autowired
  private AppService appService;
  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private RoleInitializationService roleInitializationService;
  @Autowired
  private RolePermissionService rolePermissionService;

  @RequestMapping(value = "/appnamespaces/public", method = RequestMethod.GET)
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceService.findPublicAppNamespaces();
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces", method = RequestMethod.GET)
  public List<NamespaceBO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {

    return namespaceService.findNamespaceBOs(appId, Env.valueOf(env), clusterName);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.GET)
  public NamespaceBO findNamespace(@PathVariable String appId, @PathVariable String env,
                                   @PathVariable String clusterName, @PathVariable String namespaceName) {

    return namespaceService.loadNamespaceBO(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
      method = RequestMethod.GET)
  public NamespaceBO findPublicNamespaceForAssociatedNamespace(@PathVariable String env,
                                                               @PathVariable String appId,
                                                               @PathVariable String namespaceName,
                                                               @PathVariable String clusterName) {

    return namespaceService.findPublicNamespaceForAssociatedNamespace(Env.valueOf(env), appId, clusterName, namespaceName);
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/namespaces", method = RequestMethod.POST)
  public ResponseEntity<Void> createNamespace(@PathVariable String appId,
                                              @RequestBody List<NamespaceCreationModel> models) {

    checkModel(!CollectionUtils.isEmpty(models));
    roleInitializationService.initNamespaceRoles(appId, models.get(0).getNamespace().getNamespaceName());

    String namespaceName = null;
    for (NamespaceCreationModel model : models) {
      NamespaceDTO namespace = model.getNamespace();
      namespaceName = namespace.getNamespaceName();
      RequestPrecondition
              .checkArgumentsNotEmpty(model.getEnv(), namespace.getAppId(), namespace.getClusterName(),
                      namespace.getNamespaceName());

      try {
        // TODO: 16/6/17 某些环境创建失败,统一处理这种场景
        namespaceService.createNamespace(Env.valueOf(model.getEnv()), namespace);
      } catch (Exception e) {
        logger.error("create namespace fail.", e);
        Tracer.logError(
                String.format("create namespace fail. (env=%s namespace=%s)", model.getEnv(),
                        namespace.getNamespaceName()), e);
      }
    }

    //default assign modify、release namespace role to namespace creator
    String loginUser = userInfoHolder.getUser().getUserId();
    rolePermissionService
            .assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE),
                    Sets.newHashSet(loginUser), loginUser);
    rolePermissionService
            .assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE),
                    Sets.newHashSet(loginUser), loginUser);

    return ResponseEntity.ok().build();
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteNamespace(@PathVariable String appId, @PathVariable String env,
                                              @PathVariable String clusterName, @PathVariable String namespaceName) {
    namespaceService.deleteNamespace(appId, Env.valueOf(env), clusterName, namespaceName);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateAppNamespacePermission(#appId, #appNamespace)")
  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public AppNamespace createAppNamespace(@PathVariable String appId, @RequestBody AppNamespace appNamespace) {

    RequestPrecondition.checkArgumentsNotEmpty(appNamespace.getAppId(), appNamespace.getName());
    if (!InputValidator.isValidAppNamespace(appNamespace.getName())) {
      throw new BadRequestException(String.format("Namespace格式错误: %s",
              InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE + " & "
                      + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
    }

    //add app org id as prefix
    App app = appService.load(appId);
    StringBuilder appNamespaceName = new StringBuilder();
    //add prefix postfix
    appNamespaceName
            .append(appNamespace.isPublic() ? app.getOrgId() + "." : "")
            .append(appNamespace.getName())
            .append(appNamespace.formatAsEnum() == ConfigFileFormat.Properties ? "" : "." + appNamespace.getFormat());
    appNamespace.setName(appNamespaceName.toString());

    String operator = userInfoHolder.getUser().getUserId();
    if (StringUtils.isEmpty(appNamespace.getDataChangeCreatedBy())) {
      appNamespace.setDataChangeCreatedBy(operator);
    }
    appNamespace.setDataChangeLastModifiedBy(operator);
    AppNamespace createdAppNamespace = appNamespaceService.createAppNamespaceInLocal(appNamespace);

    publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));

    return createdAppNamespace;
  }

  /**
   * env -> cluster -> cluster has not published namespace?
   * Example:
   * dev ->
   *  default -> true   (default cluster has not published namespace)
   *  customCluster -> false (customCluster cluster's all namespaces had published)
   */
  @RequestMapping(value = "/apps/{appId}/namespaces/publish_info", method = RequestMethod.GET)
  public Map<String, Map<String, Boolean>> getNamespacesPublishInfo(@PathVariable String appId) {
    return namespaceService.getNamespacesPublishInfo(appId);
  }

}
