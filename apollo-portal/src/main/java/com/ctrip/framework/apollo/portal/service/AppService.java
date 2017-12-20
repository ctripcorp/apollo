package com.ctrip.framework.apollo.portal.service;

import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Favorite;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.portal.repository.AppRepository;
import com.ctrip.framework.apollo.portal.repository.FavoriteRepository;
import com.ctrip.framework.apollo.portal.repository.PermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RolePermissionRepository;
import com.ctrip.framework.apollo.portal.repository.RoleRepository;
import com.ctrip.framework.apollo.portal.repository.UserRoleRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AppService {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AdminServiceAPI.AppAPI appAPI;
  @Autowired
  private AppRepository appRepository;
  @Autowired
  private ClusterService clusterService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private RoleInitializationService roleInitializationService;
  @Autowired
  private UserService userService;
  @Autowired
  private AppNamespaceRepository appNamespaceRepository;
  @Autowired
  private FavoriteRepository favoriteRepository;
  @Autowired
  private PermissionRepository permissionRepository;
  @Autowired
  private RolePermissionRepository rolePermissionRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private UserRoleRepository userRoleRepository;
  @Autowired
  private ConsumerRoleRepository consumerRoleRepository;

  private static Logger logger = LoggerFactory.getLogger(AppService.class);

  public List<App> findAll() {
    Iterable<App> apps = appRepository.findAll();
    if (apps == null) {
      return Collections.emptyList();
    }
    return Lists.newArrayList((apps));
  }

  public List<App> findByAppIds(Set<String> appIds) {
    return appRepository.findByAppIdIn(appIds);
  }

  public List<App> findByOwnerName(String ownerName, Pageable page) {
    return appRepository.findByOwnerName(ownerName, page);
  }

  public App load(String appId) {
    return appRepository.findByAppId(appId);
  }

  public AppDTO load(Env env, String appId) {
    return appAPI.loadApp(env, appId);
  }

  public void createAppInRemote(Env env, App app) {
    String username = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(username);
    app.setDataChangeLastModifiedBy(username);

    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, app);
    appAPI.createApp(env, appDTO);
  }

  @Transactional
  public App createAppInLocal(App app) {
    String appId = app.getAppId();
    App managedApp = appRepository.findByAppId(appId);

    if (managedApp != null) {
      throw new BadRequestException(String.format("App already exists. AppId = %s", appId));
    }

    UserInfo owner = userService.findByUserId(app.getOwnerName());
    if (owner == null) {
      throw new BadRequestException("Application's owner not exist.");
    }
    app.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(operator);
    app.setDataChangeLastModifiedBy(operator);
    App createdApp = appRepository.save(app);

    appNamespaceService.createDefaultAppNamespace(appId);
    roleInitializationService.initAppRoles(createdApp);

    Tracer.logEvent(TracerEventType.CREATE_APP, appId);

    return createdApp;
  }

  @Transactional
  public App updateAppInLocal(App app) {
    String appId = app.getAppId();

    App managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }

    managedApp.setName(app.getName());
    managedApp.setOrgId(app.getOrgId());
    managedApp.setOrgName(app.getOrgName());

    String ownerName = app.getOwnerName();
    UserInfo owner = userService.findByUserId(ownerName);
    if (owner == null) {
      throw new BadRequestException(String.format("App's owner not exists. owner = %s", ownerName));
    }
    managedApp.setOwnerName(owner.getUserId());
    managedApp.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    managedApp.setDataChangeLastModifiedBy(operator);

    return appRepository.save(managedApp);
  }

  public EnvClusterInfo createEnvNavNode(Env env, String appId) {
    EnvClusterInfo node = new EnvClusterInfo(env);
    node.setClusters(clusterService.findClusters(env, appId));
    return node;
  }

  @Transactional
  public void deleteByAppId(String appId) {
    logger.info("get appid :{}",appId);
    App app = appRepository.findByAppId(appId);
    Assert.notNull(app, "app not exist");
    logger.info("find app:{}",app);
    //1 逻辑删除app
    logger.info("delete app");
    appRepository.deleteByAppId(appId);

    //2.逻辑删除appNamespace
    AppNamespace appNamespace = appNamespaceRepository.findByAppId(appId);
    if (Objects.nonNull(appNamespace)) {
      logger.info("delete app name space");
      appNamespaceRepository.delete(appNamespace.getId());
    }
    //3.逻辑删除favorite
    Favorite favorite = favoriteRepository.findByAppId(appId);
    if (Objects.nonNull(favorite)) {
      logger.info("delete favourite");
      favoriteRepository.delete(favorite.getId());
    }

    List<Integer> permissionIds = permissionRepository.findPermissionId(appId);

    //4.逻辑删除Permission
    logger.info("delete permission");
    permissionRepository.deleteById(permissionIds);
    //5.逻辑删除RolePermission
    logger.info("delete RolePermission");
    rolePermissionRepository.deleteById(permissionIds);

    List<Integer> roleIds = roleRepository.findByAppId(appId);

    //6.删除UserRole
    logger.info("delete UserRole");
    userRoleRepository.deleteByRoleId(roleIds);

    //7.删除ConsumerRole
    logger.info("delete ConsumerRole");
    consumerRoleRepository.deleteByRoleId(roleIds);
  }
}
