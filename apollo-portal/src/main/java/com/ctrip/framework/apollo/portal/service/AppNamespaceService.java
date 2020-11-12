package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用名称空间 Service层
 */
@Service
public class AppNamespaceService {

  private static final int PRIVATE_APP_NAMESPACE_NOTIFICATION_COUNT = 5;
  private static final Joiner APP_NAMESPACE_JOINER = Joiner.on(",").skipNulls();

  private final UserInfoHolder userInfoHolder;
  private final AppNamespaceRepository appNamespaceRepository;
  private final RoleInitializationService roleInitializationService;
  private final AppService appService;
  private final RolePermissionService rolePermissionService;

  public AppNamespaceService(
      final UserInfoHolder userInfoHolder,
      final AppNamespaceRepository appNamespaceRepository,
      final RoleInitializationService roleInitializationService,
      final @Lazy AppService appService,
      final RolePermissionService rolePermissionService) {
    this.userInfoHolder = userInfoHolder;
    this.appNamespaceRepository = appNamespaceRepository;
    this.roleInitializationService = roleInitializationService;
    this.appService = appService;
    this.rolePermissionService = rolePermissionService;
  }

  /**
   * 获取公共的名称空间列表,能被其它项目关联到的app ns
   */
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceRepository.findByIsPublicTrue();
  }

  /**
   * 获取公共的名称空间信息
   *
   * @param namespaceName 名称空间名称
   * @return 公共的名称空间信息
   */
  public AppNamespace findPublicAppNamespace(String namespaceName) {
    List<AppNamespace> appNamespaces = appNamespaceRepository
        .findByNameAndIsPublic(namespaceName, true);

    if (CollectionUtils.isEmpty(appNamespaces)) {
      return null;
    }

    return appNamespaces.get(0);
  }

  /**
   * 获取指定名称空间私有的应用名称空间列表
   *
   * @param namespaceName 名称空间名称
   * @return 指定名称空间私有的应用名称空间列表
   */
  private List<AppNamespace> findAllPrivateAppNamespaces(String namespaceName) {
    return appNamespaceRepository.findByNameAndIsPublic(namespaceName, false);
  }

  /**
   * 获取指定应用的名称空间信息
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 指定应用的名称空间信息
   */
  public AppNamespace findByAppIdAndName(String appId, String namespaceName) {
    return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
  }

  /**
   * 获取指定应用id的名称空间列表
   *
   * @param appId 应用id
   * @return 指定应用id的名称空间列表
   */
  public List<AppNamespace> findByAppId(String appId) {
    return appNamespaceRepository.findByAppId(appId);
  }

  /**
   * 创建默认的应用名称空间
   *
   * @param appId 应用id
   */
  @Transactional(rollbackFor = Exception.class)
  public void createDefaultAppNamespace(String appId) {
    // 校验 `name` 在 App 下唯一
    if (!isAppNamespaceNameUnique(appId, ConfigConsts.NAMESPACE_APPLICATION)) {
      throw new BadRequestException(
          String.format("App already has application namespace. AppId = %s", appId));
    }

    // 创建 AppNamespace 对象
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
    appNs.setComment("default app namespace");
    appNs.setFormat(ConfigFileFormat.Properties.getValue());
    // 设置 AppNamespace 的创建和修改人为当前管理员
    String userId = userInfoHolder.getUser().getUserId();
    appNs.setDataChangeCreatedBy(userId);
    appNs.setDataChangeLastModifiedBy(userId);
    // 保存 AppNamespace 到数据库
    appNamespaceRepository.save(appNs);
  }

  /**
   * 判断应用名称空间名称是否唯一
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return true, 名称空间名称唯一，否则，false
   */
  public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(namespaceName, "Namespace must not be null");
    return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
  }

  /**
   * 创建应用名称空间
   *
   * @param appNamespace 应用名称空间
   * @return 创建的应用名称空间信息
   */
  public AppNamespace createAppNamespaceInLocal(AppNamespace appNamespace) {
    return createAppNamespaceInLocal(appNamespace, true);
  }

  /**
   * 创建应用名称空间
   *
   * @param appNamespace          应用名称空间
   * @param appendNamespacePrefix 追加的后缀
   * @return 创建的应用名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public AppNamespace createAppNamespaceInLocal(AppNamespace appNamespace,
      boolean appendNamespacePrefix) {
    String appId = appNamespace.getAppId();

    // 校验对应的 App 是否存在。若不存在，抛出 BadRequestException 异常
    //add app org id as prefix
    App app = appService.load(appId);
    if (app == null) {
      throw new BadRequestException("App not exist. AppId = " + appId);
    }

    // public namespaces only allow properties format
    if (appNamespace.isPublic()) {
      appNamespace.setFormat(ConfigFileFormat.Properties.getValue());
    }

    // 拼接 AppNamespace 的 `name` 属性。
    StringBuilder appNamespaceName = new StringBuilder();
    //add prefix postfix
    appNamespaceName.append(
        // 公用类型，拼接组织编号
        appNamespace.isPublic() && appendNamespacePrefix ? app.getOrgId() + "." : "")
        .append(appNamespace.getName()).append(appNamespace.formatAsEnum() == ConfigFileFormat
        .Properties ? "" : "." + appNamespace.getFormat());
    appNamespace.setName(appNamespaceName.toString());

    // 设置 AppNamespace 的 `comment` 属性为空串，若为 null 。
    if (appNamespace.getComment() == null) {
      appNamespace.setComment("");
    }

    // 校验 AppNamespace 的 `format` 是否合法
    if (!ConfigFileFormat.isValidFormat(appNamespace.getFormat())) {
      throw new BadRequestException(
          "Invalid namespace format. format must be properties、json、yaml、yml、xml");
    }

    // 设置 AppNamespace 的创建和修改人
    String operator = appNamespace.getDataChangeCreatedBy();
    if (StringUtils.isBlank(operator)) {
      // 当前登录管理员
      operator = userInfoHolder.getUser().getUserId();
      appNamespace.setDataChangeCreatedBy(operator);
    }

    appNamespace.setDataChangeLastModifiedBy(operator);

    // globally uniqueness check for public app namespace
    // 公用类型，校验 `name` 在全局唯一
    if (appNamespace.isPublic()) {
      checkAppNamespaceGlobalUniqueness(appNamespace);
    } else {
      // 私有类型，校验 `name` 在 App 下唯一
      // check private app namespace
      if (appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName())
          != null) {
        throw new BadRequestException(
            "Private AppNamespace " + appNamespace.getName() + " already exists!");
      }
      // should not have the same with public app namespace
      checkPublicAppNamespaceGlobalUniqueness(appNamespace);
    }

    // 保存 AppNamespace 到数据库
    AppNamespace createdAppNamespace = appNamespaceRepository.save(appNamespace);

    // 初始化 NamespaceRole，各环境名称空间角色
    roleInitializationService
        .initNamespaceRoles(appNamespace.getAppId(), appNamespace.getName(), operator);
    roleInitializationService
        .initNamespaceEnvRoles(appNamespace.getAppId(), appNamespace.getName(), operator);

    return createdAppNamespace;
  }

  /**
   * 校验应用名称空间全局唯一性
   *
   * @param appNamespace
   */
  private void checkAppNamespaceGlobalUniqueness(AppNamespace appNamespace) {
    // 校验公有应用名称空间全局唯一性
    checkPublicAppNamespaceGlobalUniqueness(appNamespace);

    // 私有的应用名称空间列表
    List<AppNamespace> privateAppNamespaces = findAllPrivateAppNamespaces(appNamespace.getName());
    if (CollectionUtils.isNotEmpty(privateAppNamespaces)) {
      Set<String> appIds = Sets.newHashSet();
      for (AppNamespace ans : privateAppNamespaces) {
        appIds.add(ans.getAppId());
        if (appIds.size() == PRIVATE_APP_NAMESPACE_NOTIFICATION_COUNT) {
          break;
        }
      }

      throw new BadRequestException(
          "Public AppNamespace " + appNamespace.getName()
              + " already exists as private AppNamespace in appId: "
              + APP_NAMESPACE_JOINER.join(appIds) + ", etc. Please select another name!");
    }
  }

  /**
   * 校验公有应用名称空间全局唯一性
   *
   * @param appNamespace 应用名称空间
   */
  private void checkPublicAppNamespaceGlobalUniqueness(AppNamespace appNamespace) {
    AppNamespace publicAppNamespace = findPublicAppNamespace(appNamespace.getName());
    if (publicAppNamespace != null) {
      throw new BadRequestException("AppNamespace " + appNamespace.getName()
          + " already exists as public namespace in appId: " + publicAppNamespace.getAppId() + "!");
    }
  }

  /**
   * 删除应用名称空间
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 待删除的应用名称空间信息
   */
  @Transactional(rollbackFor = Exception.class)
  public AppNamespace deleteAppNamespace(String appId, String namespaceName) {
    AppNamespace appNamespace = appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
    if (appNamespace == null) {
      throw new BadRequestException(
          String.format("AppNamespace not exists. AppId = %s, NamespaceName = %s", appId,
              namespaceName));
    }

    String operator = userInfoHolder.getUser().getUserId();

    // 这个操作者会传递给com.ctrip.framework.apollo.portal.listener.DeletionListener.onAppNamespaceDeletionEvent
    appNamespace.setDataChangeLastModifiedBy(operator);

    // 删除portal数据库中的应用名称空间
    appNamespaceRepository.delete(appId, namespaceName, operator);

    // 删除portal数据库中Permission、Role，和应用名称空间相关数据
    rolePermissionService.deleteRolePermissionsByAppIdAndNamespace(appId, namespaceName, operator);

    return appNamespace;
  }

  /**
   * 通过应用id批量删除
   *
   * @param appId    应用id
   * @param operator 操作人
   */
  public void batchDeleteByAppId(String appId, String operator) {
    appNamespaceRepository.batchDeleteByAppId(appId, operator);
  }
}
