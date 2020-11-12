package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.http.MultiResponseEntity;
import com.ctrip.framework.apollo.common.http.RichResponseEntity;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.model.AppModel;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.listener.AppCreationEvent;
import com.ctrip.framework.apollo.portal.listener.AppDeletionEvent;
import com.ctrip.framework.apollo.portal.listener.AppInfoChangedEvent;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 应用 Controller层
 */
@RestController
@RequestMapping("/apps")
public class AppController {

  private final UserInfoHolder userInfoHolder;
  private final AppService appService;
  private final PortalSettings portalSettings;
  private final ApplicationEventPublisher publisher;
  private final RolePermissionService rolePermissionService;
  private final RoleInitializationService roleInitializationService;

  public AppController(
      final UserInfoHolder userInfoHolder,
      final AppService appService,
      final PortalSettings portalSettings,
      final ApplicationEventPublisher publisher,
      final RolePermissionService rolePermissionService,
      final RoleInitializationService roleInitializationService) {
    this.userInfoHolder = userInfoHolder;
    this.appService = appService;
    this.portalSettings = portalSettings;
    this.publisher = publisher;
    this.rolePermissionService = rolePermissionService;
    this.roleInitializationService = roleInitializationService;
  }

  /**
   * 获取应用信息
   *
   * @param appIds 应用信息的AppID
   * @return 应用信息
   */
  @GetMapping
  public List<App> findApps(@RequestParam(value = "appIds", required = false) String appIds) {
    if (StringUtils.isEmpty(appIds)) {
      return appService.findAll();
    }
    return appService.findByAppIds(Sets.newHashSet(appIds.split(",")));
  }

  /**
   * 通过应用名称或者应用id查询应用信息分页列表
   *
   * @param query    应用名称或者应用id
   * @param pageable 分页
   * @return 应用信息分页列表
   */
  @GetMapping("/search/by-appid-or-name")
  public PageDTO<App> searchByAppIdOrAppName(
      @RequestParam(value = "query", required = false) String query,
      Pageable pageable) {
    if (StringUtils.isEmpty(query)) {
      return appService.findAll(pageable);
    }
    return appService.searchByAppIdOrAppName(query, pageable);
  }

  /**
   * 获取所有者app信息列表
   *
   * @param owner 所有者
   * @param page  分页信息
   * @return 所有者的app信息列表
   */
  @GetMapping("/by-owner")
  public List<App> findAppsByOwner(@RequestParam("owner") String owner, Pageable page) {
    Set<String> appIds = Sets.newHashSet();
    // 查找所有者的角色信息
    List<Role> userRoles = rolePermissionService.findUserRoles(owner);

    // 通过角色名称提取应用id列表
    for (Role role : userRoles) {
      String appId = RoleUtils.extractAppIdFromRoleName(role.getRoleName());

      if (appId != null) {
        appIds.add(appId);
      }
    }
    // 找到应用列表信息
    return appService.findByAppIds(appIds, page);
  }

  /**
   * 创建应用
   *
   * @param appModel 应用信息
   * @return 创建的应用信息
   */
  @PreAuthorize(value = "@permissionValidator.hasCreateApplicationPermission()")
  @PostMapping
  public App create(@Valid @RequestBody AppModel appModel) {
    // 将 AppModel 转换成 App 对象
    App app = transformToApp(appModel);
    // 保存 App 对象到数据库
    App createdApp = appService.createAppInLocal(app);
    // 发布 AppCreationEvent 创建事件
    publisher.publishEvent(new AppCreationEvent(createdApp));
    // 授予 App 管理员的角色
    Set<String> admins = appModel.getAdmins();
    if (!CollectionUtils.isEmpty(admins)) {
      rolePermissionService.assignRoleToUsers(RoleUtils.buildAppMasterRoleName(
          createdApp.getAppId()), admins, userInfoHolder.getUser().getUserId());
    }
    // 返回 App 对象
    return createdApp;
  }

  /**
   * 更新应用
   *
   * @param appId    应用id
   * @param appModel 更新的应用信息
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @PutMapping("/{appId:.+}")
  public void update(@PathVariable String appId, @Valid @RequestBody AppModel appModel) {
    if (!Objects.equals(appId, appModel.getAppId())) {
      throw new BadRequestException("The App Id of path variable and request body is different");
    }
    // 将 AppModel 转换成 App 对象
    App app = transformToApp(appModel);
    // 更新 App 对象到数据库
    App updatedApp = appService.updateAppInLocal(app);
    // 发布 AppCreationEvent 更新事件
    publisher.publishEvent(new AppInfoChangedEvent(updatedApp));
  }

  /**
   * 应用的导航树
   *
   * @param appId 应用id
   * @return 环境集群信息
   */
  @GetMapping("/{appId}/navtree")
  public MultiResponseEntity<EnvClusterInfo> nav(@PathVariable String appId) {

    MultiResponseEntity<EnvClusterInfo> response = MultiResponseEntity.ok();
    // 环境列表
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        // 构建导航树节点
        response.addResponseEntity(RichResponseEntity.ok(appService.createEnvNavNode(env, appId)));
      } catch (Exception e) {
        response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
            "load env:" + env.getName() + " cluster error." + e.getMessage()));
      }
    }
    return response;
  }

  /**
   * 创建应用信息
   *
   * @param env 环境
   * @param app 应用信息
   * @return 创建成功，ResponseEntity.ok()
   */
  @PostMapping(value = "/envs/{env}", consumes = {"application/json"})
  public ResponseEntity<Void> create(@PathVariable String env, @Valid @RequestBody App app) {
    // 创建 App 对象到数据库
    appService.createAppInRemote(Env.valueOf(env), app);
    // 初始化命名空间特定环境的角色
    roleInitializationService.initNamespaceSpecificEnvRoles(app.getAppId(),
        ConfigConsts.NAMESPACE_APPLICATION, env, userInfoHolder.getUser().getUserId());

    return ResponseEntity.ok().build();
  }

  /**
   * 加载应用信息
   *
   * @param appId 应用id
   * @return 加载的应用信息
   */
  @GetMapping("/{appId:.+}")
  public App load(@PathVariable String appId) {

    return appService.load(appId);
  }

  /**
   * 删除应用
   *
   * @param appId 应用id
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping("/{appId:.+}")
  public void deleteApp(@PathVariable String appId) {
    // 删除 App 对象同步到数据库
    App app = appService.deleteAppInLocal(appId);
    // 发布 AppCreationEvent 更新事件
    publisher.publishEvent(new AppDeletionEvent(app));
  }

  /**
   * 查询丢失的环境
   *
   * @param appId 应用id
   * @return 加载环境时报错的环境列表
   */
  @GetMapping("/{appId}/miss_envs")
  public MultiResponseEntity<String> findMissEnvs(@PathVariable String appId) {

    MultiResponseEntity<String> response = MultiResponseEntity.ok();
    for (Env env : portalSettings.getActiveEnvs()) {
      try {
        // 加载环境
        appService.load(env, appId);
      } catch (Exception e) {
        // 未找到环境
        if (e instanceof HttpClientErrorException
            && ((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
          response.addResponseEntity(RichResponseEntity.ok(env.toString()));
        } else {
          // 服务错误
          response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
              String.format("load appId:%s from env %s error.", appId,
                  env)
                  + e.getMessage()));
        }
      }
    }

    return response;

  }

  /**
   * 将 AppModel 转换成 App 对象
   *
   * @param appModel 应用model
   * @return 应用对象
   */
  private App transformToApp(AppModel appModel) {
    String appId = appModel.getAppId();
    String appName = appModel.getName();
    String ownerName = appModel.getOwnerName();
    String orgId = appModel.getOrgId();
    String orgName = appModel.getOrgName();
    return new App(appId, appName, ownerName, orgId, orgName);
  }
}
