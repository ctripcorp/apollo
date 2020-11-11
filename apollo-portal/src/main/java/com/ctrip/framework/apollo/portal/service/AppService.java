package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.repository.AppRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用 Service
 */
@Service
public class AppService {

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.AppAPI appAPI;
  private final AppRepository appRepository;
  private final ClusterService clusterService;
  private final AppNamespaceService appNamespaceService;
  private final RoleInitializationService roleInitializationService;
  private final RolePermissionService rolePermissionService;
  private final FavoriteService favoriteService;
  private final UserService userService;

  public AppService(
      final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.AppAPI appAPI,
      final AppRepository appRepository,
      final ClusterService clusterService,
      final AppNamespaceService appNamespaceService,
      final RoleInitializationService roleInitializationService,
      final RolePermissionService rolePermissionService,
      final FavoriteService favoriteService,
      final UserService userService) {
    this.userInfoHolder = userInfoHolder;
    this.appAPI = appAPI;
    this.appRepository = appRepository;
    this.clusterService = clusterService;
    this.appNamespaceService = appNamespaceService;
    this.roleInitializationService = roleInitializationService;
    this.rolePermissionService = rolePermissionService;
    this.favoriteService = favoriteService;
    this.userService = userService;
  }

  /**
   * 找到全部的应用信息
   *
   * @return 应用信息集合
   */
  public List<App> findAll() {
    Iterable<App> apps = appRepository.findAll();
    if (apps == null) {
      return Collections.emptyList();
    }
    return Lists.newArrayList((apps));
  }

  /**
   * 应用信息分页
   *
   * @return 应用信息分页集合
   */
  public PageDTO<App> findAll(Pageable pageable) {
    Page<App> apps = appRepository.findAll(pageable);

    return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
  }

  /**
   * 通过应用名称或者应用id查询应用信息分页列表
   *
   * @param query    应用名称或者应用id
   * @param pageable 分页
   * @return 应用信息分页列表
   */
  public PageDTO<App> searchByAppIdOrAppName(String query, Pageable pageable) {
    Page<App> apps = appRepository.findByAppIdContainingOrNameContaining(query, query, pageable);
    return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
  }

  /**
   * 通过appId集合找到应用信息
   *
   * @param appIds appId集合
   * @return 应用信息集合
   */
  public List<App> findByAppIds(Set<String> appIds) {
    return appRepository.findByAppIdIn(appIds);
  }

  /**
   * 获取应用id列表的应用信息列表
   *
   * @param appIds   应用信息的AppID
   * @param pageable 分页对象
   * @return 应用信息
   */
  public List<App> findByAppIds(Set<String> appIds, Pageable pageable) {
    return appRepository.findByAppIdIn(appIds, pageable);
  }

  /**
   * 获取所有人的应用信息列表
   *
   * @param ownerName 所有人名称
   * @param page      分页对象
   * @return 应用信息
   */
  public List<App> findByOwnerName(String ownerName, Pageable page) {
    return appRepository.findByOwnerName(ownerName, page);
  }

  /**
   * 加载应用信息
   *
   * @param appId 应用id
   * @return 加载的应用信息
   */
  public App load(String appId) {
    return appRepository.findByAppId(appId);
  }

  /**
   * 加载应用信息
   *
   * @param env   环境
   * @param appId 应用id
   * @return 应用信息
   */
  public AppDTO load(Env env, String appId) {
    return appAPI.loadApp(env, appId);
  }

  /**
   * 远程创建应用
   *
   * @param env 环境
   * @param app 应用信息DTO
   * @return 创建的应用信息
   */
  public void createAppInRemote(Env env, App app) {
    String username = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(username);
    app.setDataChangeLastModifiedBy(username);

    AppDTO appDTO = BeanUtils.transform(AppDTO.class, app);
    appAPI.createApp(env, appDTO);
  }

  /**
   * 创建应用
   *
   * @param app 应用信息
   * @return 创建的应用信息
   */
  @Transactional(rollbackFor = Exception.class)
  public App createAppInLocal(App app) {
    String appId = app.getAppId();
    // 判断 `appId` 是否已经存在对应的 App 对象。若已经存在，抛出 BadRequestException 异常。
    App managedApp = appRepository.findByAppId(appId);

    if (managedApp != null) {
      throw new BadRequestException(String.format("App already exists. AppId = %s", appId));
    }
    // 获得 UserInfo 对象。若不存在，抛出 BadRequestException 异常
    UserInfo owner = userService.findByUserId(app.getOwnerName());
    if (owner == null) {
      throw new BadRequestException("Application's owner not exist.");
    }
    app.setOwnerEmail(owner.getEmail());

    // 设置 App 的创建和修改人
    String operator = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(operator);
    app.setDataChangeLastModifiedBy(operator);

    // 保存 App 对象到数据库
    App createdApp = appRepository.save(app);

    // 创建 App 的默认命名空间 "application"
    appNamespaceService.createDefaultAppNamespace(appId);
    // 初始化 App 角色
    roleInitializationService.initAppRoles(createdApp);

    Tracer.logEvent(TracerEventType.CREATE_APP, appId);

    return createdApp;
  }

  /**
   * 更新应用
   *
   * @param app 应用信息
   */
  @Transactional(rollbackFor = Exception.class)
  public App updateAppInLocal(App app) {
    String appId = app.getAppId();
    // 判断 `appId` 是否已经存在对应的 App 对象。若已经存在，抛出 BadRequestException 异常。
    App managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }

    managedApp.setName(app.getName());
    managedApp.setOrgId(app.getOrgId());
    managedApp.setOrgName(app.getOrgName());

    String ownerName = app.getOwnerName();
    // 获得 UserInfo 对象。若不存在，抛出 BadRequestException 异常
    UserInfo owner = userService.findByUserId(ownerName);
    if (owner == null) {
      throw new BadRequestException(String.format("App's owner not exists. owner = %s", ownerName));
    }
    managedApp.setOwnerName(owner.getUserId());
    managedApp.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    managedApp.setDataChangeLastModifiedBy(operator);

    // 保存
    return appRepository.save(managedApp);
  }

  /**
   * 构建环境导航树节点
   *
   * @param env   环境
   * @param appId 应用id
   * @return 构建的环境导航树节点
   */
  public EnvClusterInfo createEnvNavNode(Env env, String appId) {
    EnvClusterInfo node = new EnvClusterInfo(env);
    node.setClusters(clusterService.findClusters(env, appId));
    return node;
  }

  /**
   * 删除应用
   *
   * @param appId 应用id
   */
  @Transactional(rollbackFor = Exception.class)
  public App deleteAppInLocal(String appId) {
    App managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }
    String operator = userInfoHolder.getUser().getUserId();

    //this operator is passed to com.ctrip.framework.apollo.portal.listener.DeletionListener.onAppDeletionEvent
    managedApp.setDataChangeLastModifiedBy(operator);

    //删除portal数据库中的app
    appRepository.deleteApp(appId, operator);

    //删除portal数据库中的appNamespace
    appNamespaceService.batchDeleteByAppId(appId, operator);

    //删除portal数据库中的收藏表
    favoriteService.batchDeleteByAppId(appId, operator);

    //删除portal数据库中Permission、Role相关数据
    rolePermissionService.deleteRolePermissionsByAppId(appId, operator);

    return managedApp;
  }
}
