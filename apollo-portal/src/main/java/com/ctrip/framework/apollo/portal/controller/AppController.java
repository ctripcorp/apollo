package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.http.MultiResponseEntity;
import com.ctrip.framework.apollo.common.http.RichResponseEntity;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.model.AppModel;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.listener.AppCreationEvent;
import com.ctrip.framework.apollo.portal.listener.AppDeletionEvent;
import com.ctrip.framework.apollo.portal.listener.AppInfoChangedEvent;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;


@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AppService appService;
  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private RolePermissionService rolePermissionService;

  @RequestMapping(value = "", method = RequestMethod.GET)
  public List<App> findApps(@RequestParam(value = "appIds", required = false) String appIds) {
    if (StringUtils.isEmpty(appIds)) {
      return appService.findAll();
    } else {
      return appService.findByAppIds(Sets.newHashSet(appIds.split(",")));
    }

  }

  @RequestMapping(value = "/by-owner", method = RequestMethod.GET)
  public List<App> findAppsByOwner(@RequestParam("owner") String owner, Pageable page) {
    return appService.findByOwnerName(owner, page);
  }

  @RequestMapping(value = "", method = RequestMethod.POST)
  public App create(@RequestBody AppModel appModel) {

    App app = transformToApp(appModel);

    App createdApp = appService.createAppInLocal(app);

    publisher.publishEvent(new AppCreationEvent(createdApp));

    Set<String> admins = appModel.getAdmins();
    if (!CollectionUtils.isEmpty(admins)) {
      rolePermissionService
          .assignRoleToUsers(RoleUtils.buildAppMasterRoleName(createdApp.getAppId()),
              admins, userInfoHolder.getUser().getUserId());
    }

    return createdApp;
  }

  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @RequestMapping(value = "/{appId:.+}", method = RequestMethod.PUT)
  public void update(@PathVariable String appId, @RequestBody AppModel appModel) {
    if (!Objects.equals(appId, appModel.getAppId())) {
      throw new BadRequestException("The App Id of path variable and request body is different");
    }

    App app = transformToApp(appModel);

    App updatedApp = appService.updateAppInLocal(app);

    publisher.publishEvent(new AppInfoChangedEvent(updatedApp));
  }

  @RequestMapping(value = "/{appId}/navtree", method = RequestMethod.GET)
  public MultiResponseEntity<EnvClusterInfo> nav(@PathVariable String appId) {

    MultiResponseEntity<EnvClusterInfo> response = MultiResponseEntity.ok();
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        response.addResponseEntity(RichResponseEntity.ok(appService.createEnvNavNode(env, appId)));
      } catch (Exception e) {
        response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
            "load env:" + env.name() + " cluster error." + e
                .getMessage()));
      }
    }
    return response;
  }

  @RequestMapping(value = "/envs/{env}", method = RequestMethod.POST, consumes = {
      "application/json"})
  public ResponseEntity<Void> create(@PathVariable String env, @RequestBody App app) {

    RequestPrecondition.checkArgumentsNotEmpty(app.getName(), app.getAppId(), app.getOwnerEmail(),
        app.getOwnerName(),
        app.getOrgId(), app.getOrgName());
    if (!InputValidator.isValidClusterNamespace(app.getAppId())) {
      throw new BadRequestException(InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE);
    }

    appService.createAppInRemote(Env.valueOf(env), app);

    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/{appId:.+}", method = RequestMethod.GET)
  public App load(@PathVariable String appId) {

    return appService.load(appId);
  }


  @RequestMapping(value = "/{appId:.+}", method = RequestMethod.DELETE)
  public void deleteApp(@PathVariable String appId) {

    App app = appService.load(appId);

    publisher.publishEvent(new AppDeletionEvent(app));

    appService.deleteAppInLocal(appId);
  }

  @RequestMapping(value = "/{appId}/miss_envs", method = RequestMethod.GET)
  public MultiResponseEntity<Env> findMissEnvs(@PathVariable String appId) {

    MultiResponseEntity<Env> response = MultiResponseEntity.ok();
    for (Env env : portalSettings.getActiveEnvs()) {
      try {
        appService.load(env, appId);
      } catch (Exception e) {
        if (e instanceof HttpClientErrorException &&
            ((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
          response.addResponseEntity(RichResponseEntity.ok(env));
        } else {
          response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
              String.format("load appId:%s from env %s error.", appId,
                  env)
                  + e.getMessage()));
        }
      }
    }

    return response;

  }

  private App transformToApp(AppModel appModel) {
    String appId = appModel.getAppId();
    String appName = appModel.getName();
    String ownerName = appModel.getOwnerName();
    String orgId = appModel.getOrgId();
    String orgName = appModel.getOrgName();

    RequestPrecondition.checkArgumentsNotEmpty(appId, appName, ownerName, orgId, orgName);

    if (!InputValidator.isValidClusterNamespace(appModel.getAppId())) {
      throw new BadRequestException(
          String.format("AppId格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }

    return App.builder()
        .appId(appId)
        .name(appName)
        .ownerName(ownerName)
        .orgId(orgId)
        .orgName(orgName)
        .build();

  }
}
