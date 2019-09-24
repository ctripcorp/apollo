package com.ctrip.framework.apollo.openapi.v1.controller;


import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.ClusterService;
import com.ctrip.framework.apollo.portal.service.NamespaceLockService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.spi.UserService;

@RestController("openapiNamespaceController")
public class NamespaceController {

  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final AppNamespaceService appNamespaceService;
  private final ApplicationEventPublisher publisher;
  private final UserService userService;
  private final ClusterService clusterService;

  public NamespaceController(final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService, final AppNamespaceService appNamespaceService,
      final ApplicationEventPublisher publisher, final UserService userService,
      final ClusterService clusterService) {
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.appNamespaceService = appNamespaceService;
    this.publisher = publisher;
    this.userService = userService;
    this.clusterService = clusterService;
  }


  @PreAuthorize(
      value = "@consumerPermissionValidator.hasCreateNamespacePermission(#request, #appId)")
  @PostMapping(value = "/openapi/v1/apps/{appId}/appnamespaces")
  public OpenAppNamespaceDTO createNamespace(@PathVariable String appId,
      @RequestBody OpenAppNamespaceDTO appNamespaceDTO, HttpServletRequest request) {

    if (!Objects.equals(appId, appNamespaceDTO.getAppId())) {
      throw new BadRequestException(
          String.format("AppId not equal. AppId in path = %s, AppId in payload = %s", appId,
              appNamespaceDTO.getAppId()));
    }
    RequestPrecondition.checkArgumentsNotEmpty(appNamespaceDTO.getAppId(),
        appNamespaceDTO.getName(), appNamespaceDTO.getFormat(),
        appNamespaceDTO.getDataChangeCreatedBy());

    if (!InputValidator.isValidAppNamespace(appNamespaceDTO.getName())) {
      throw new BadRequestException(
          String.format("Namespace格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
              + " & " + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
    }

    if (!ConfigFileFormat.isValidFormat(appNamespaceDTO.getFormat())) {
      throw new BadRequestException(
          String.format("Invalid namespace format. format = %s", appNamespaceDTO.getFormat()));
    }

    String operator = appNamespaceDTO.getDataChangeCreatedBy();
    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException(String.format("Illegal user. user = %s", operator));
    }

    AppNamespace appNamespace = OpenApiBeanUtils.transformToAppNamespace(appNamespaceDTO);
    AppNamespace createdAppNamespace = appNamespaceService.createAppNamespaceInLocal(appNamespace,
        appNamespaceDTO.isAppendNamespacePrefix());

    publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));

    return OpenApiBeanUtils.transformToOpenAppNamespaceDTO(createdAppNamespace);
  }

  @PreAuthorize(
      value = "@consumerPermissionValidator.hasCreateNamespacePermission(#request, #appId)")
  @PostMapping(value = "/openapi/v1/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces")
  public OpenNamespaceDTO createNamespaces(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @Valid @RequestBody OpenNamespaceDTO namespace,
      HttpServletRequest request) {

    if (!Objects.equals(appId, namespace.getAppId())) {
      throw new BadRequestException(
          String.format("AppId not equal. AppId in path = %s, AppId in payload = %s", appId,
              namespace.getAppId()));
    }

    String namespaceName = namespace.getNamespaceName();
    String operator = namespace.getDataChangeCreatedBy();

    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(namespaceName, operator),
        "name and dataChangeCreatedBy should not be null or empty");

    if (!InputValidator.isValidAppNamespace(namespaceName)) {
      throw new BadRequestException(
          String.format("Namespace Name 格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE
              + " & " + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
    }

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("User " + operator + " doesn't exist!");
    }

    ClusterDTO cluster = clusterService.loadCluster(appId, Env.fromString(env), clusterName);
    long parentClusterId = cluster.getParentClusterId();

    if (parentClusterId == 0) {
      createAppNamespace(appId, env, clusterName, namespace, namespaceName, operator);
    } else {
      ClusterDTO parentCluster = clusterService.loadCluster(Env.fromString(env), parentClusterId);
      String parentAppId = parentCluster.getAppId();
      AppNamespace appNamespace = appNamespaceService.findPublicAppNamespace(namespaceName);
      if (appNamespace == null) {
        if (namespace.isPublic()) {
          throw new BadRequestException("custom cluster has parent cluster, same name public namespaces need to exist in parent cluster.");
        } else {
          createAppNamespace(appId, env, clusterName, namespace, namespaceName, operator);
        }
      } else if (!parentAppId.equals(appNamespace.getAppId())) {
        throw new BadRequestException("same name public namespaces is exist.");
      } else {
        if (!namespace.isPublic()) {
          throw new BadRequestException("same name namespaces is public, and exist in parent cluster.");
        }
      }
    }

    NamespaceDTO toCreate = OpenApiBeanUtils.transformToNamespaceDTO(namespace);
    NamespaceDTO createdNamespace = namespaceService.createNamespace(Env.valueOf(env), toCreate);
    NamespaceBO namespaceBO = namespaceService.transformNamespace2BO(Env.valueOf(env), createdNamespace);
    return OpenApiBeanUtils.transformFromNamespaceBO(namespaceBO);
  }

  private void createAppNamespace(String appId, String env, String clusterName,
      OpenNamespaceDTO namespace, String namespaceName, String operator) {
    OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
    openAppNamespaceDTO.setAppId(appId);
    openAppNamespaceDTO.setName(namespaceName);
    openAppNamespaceDTO.setDataChangeCreatedBy(operator);
    openAppNamespaceDTO.setFormat(namespace.getFormat());
    openAppNamespaceDTO.setPublic(namespace.isPublic());
    AppNamespace appNamespace = OpenApiBeanUtils.transformToAppNamespace(openAppNamespaceDTO);
    appNamespaceService.createAppNamespaceInLocal(appNamespace, false);
    namespaceService.onlyCreateMissingAppNamespace(appId, env, clusterName);
  }

  @GetMapping(value = "/openapi/v1/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces")
  public List<OpenNamespaceDTO> findNamespaces(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {

    return OpenApiBeanUtils.batchTransformFromNamespaceBOs(
        namespaceService.findNamespaceBOs(appId, Env.fromString(env), clusterName));
  }

  @GetMapping(
      value = "/openapi/v1/envs/{env}/apps/{appId}/clusters/{clusterName}/full-items-namespaces")
  public List<OpenNamespaceDTO> findFullItemsNamespaces(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName) {

    return OpenApiBeanUtils.batchTransformFromNamespaceBOs(
        namespaceService.findFullItemsNamespaceBOs(appId, Env.fromString(env), clusterName));
  }

  @GetMapping(
      value = "/openapi/v1/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
  public OpenNamespaceDTO loadNamespace(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {
    NamespaceBO namespaceBO =
        namespaceService.loadNamespaceBO(appId, Env.fromString(env), clusterName, namespaceName);
    if (namespaceBO == null) {
      return null;
    }
    return OpenApiBeanUtils.transformFromNamespaceBO(namespaceBO);
  }

  @GetMapping(
      value = "/openapi/v1/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
  public OpenNamespaceLockDTO getNamespaceLock(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {

    NamespaceDTO namespace = namespaceService.loadNamespaceBaseInfo(appId, Env.fromString(env),
        clusterName, namespaceName);
    NamespaceLockDTO lockDTO = namespaceLockService.getNamespaceLock(appId, Env.fromString(env),
        clusterName, namespaceName);
    return OpenApiBeanUtils.transformFromNamespaceLockDTO(namespace.getNamespaceName(), lockDTO);
  }

}
