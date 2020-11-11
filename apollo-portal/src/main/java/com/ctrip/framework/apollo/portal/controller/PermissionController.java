package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.AppRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceEnvRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.PermissionCondition;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.service.SystemRoleManagerService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限 Controller
 */
@RestController
public class PermissionController {

  private final UserInfoHolder userInfoHolder;
  private final RolePermissionService rolePermissionService;
  private final UserService userService;
  private final RoleInitializationService roleInitializationService;
  private final SystemRoleManagerService systemRoleManagerService;
  private final PermissionValidator permissionValidator;

  @Autowired
  public PermissionController(
      final UserInfoHolder userInfoHolder,
      final RolePermissionService rolePermissionService,
      final UserService userService,
      final RoleInitializationService roleInitializationService,
      final SystemRoleManagerService systemRoleManagerService,
      final PermissionValidator permissionValidator) {
    this.userInfoHolder = userInfoHolder;
    this.rolePermissionService = rolePermissionService;
    this.userService = userService;
    this.roleInitializationService = roleInitializationService;
    this.systemRoleManagerService = systemRoleManagerService;
    this.permissionValidator = permissionValidator;
  }

  /**
   * 初始化应用权限
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 成功的响应信息
   */
  @PostMapping("/apps/{appId}/initPermission")
  public ResponseEntity<Void> initAppPermission(@PathVariable String appId,
      @RequestBody String namespaceName) {
    roleInitializationService
        .initNamespaceEnvRoles(appId, namespaceName, userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 查看指定应用下是否具有指定权限类型
   * <p>指定用户角色是否具有指定权限</p>
   *
   * @param appId          应用id
   * @param permissionType 权限类型
   * @return 成功的响应信息, true,具有指定的权限,否则 false
   */
  @GetMapping("/apps/{appId}/permissions/{permissionType}")
  public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId,
      @PathVariable String permissionType) {
    PermissionCondition permissionCondition = new PermissionCondition();
    boolean hasPermission = rolePermissionService.userHasPermission(
        userInfoHolder.getUser().getUserId(), permissionType, appId);
    permissionCondition.setHasPermission(hasPermission);

    return ResponseEntity.ok().body(permissionCondition);
  }

  /**
   * 查看应用下名称空间存在指定权限类型
   * <p>指定用户角色是否具有指定权限<权限类型+权限对象类型<应用id+名称空间名称构成>构成></p>
   *
   * @param appId          应用id
   * @param namespaceName  名称空间名称
   * @param permissionType 权限类型
   * @return 成功的响应信息, true,具有指定的权限,否则 false
   */
  @GetMapping("/apps/{appId}/namespaces/{namespaceName}/permissions/{permissionType}")
  public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId,
      @PathVariable String namespaceName,
      @PathVariable String permissionType) {
    PermissionCondition permissionCondition = new PermissionCondition();

    permissionCondition.setHasPermission(rolePermissionService
        .userHasPermission(userInfoHolder.getUser().getUserId(), permissionType,
            RoleUtils.buildNamespaceTargetId(appId, namespaceName)));
    return ResponseEntity.ok().body(permissionCondition);
  }

  /**
   * 查看指定应用指定环境的名称空间是否具有权限类型
   * <p>指定用户角色是否具有指定权限<权限类型+权限对象类型<应用id+名称空间名称+环境构成>构成></p>
   *
   * @param appId          应用id
   * @param env            环境
   * @param namespaceName  名称空间名称
   * @param permissionType 权限类型
   * @return 成功的响应信息, true,具有指定的权限,否则 false
   */
  @GetMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/permissions/{permissionType}")
  public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId,
      @PathVariable String env, @PathVariable String namespaceName,
      @PathVariable String permissionType) {
    PermissionCondition permissionCondition = new PermissionCondition();

    permissionCondition.setHasPermission(rolePermissionService
        .userHasPermission(userInfoHolder.getUser().getUserId(), permissionType,
            RoleUtils.buildNamespaceTargetId(appId, namespaceName, env)));

    return ResponseEntity.ok().body(permissionCondition);
  }

  /**
   * 是否有超级管理员权限
   *
   * @return 权限状态响应实体
   */
  @GetMapping("/permissions/root")
  public ResponseEntity<PermissionCondition> hasRootPermission() {
    PermissionCondition permissionCondition = new PermissionCondition();
    boolean isSuperAdmin = rolePermissionService.isSuperAdmin(userInfoHolder.getUser().getUserId());
    permissionCondition.setHasPermission(isSuperAdmin);
    return ResponseEntity.ok().body(permissionCondition);
  }

  /**
   * 获取名称空间环境分配的角色用户列表.
   *
   * @param appId         应用id
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @return 名称空间环境分配的角色用户列表信息
   */
  @GetMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/role_users")
  public NamespaceEnvRolesAssignedUsers getNamespaceEnvRoles(@PathVariable String appId,
      @PathVariable String env, @PathVariable String namespaceName) {

    // 验证
    if (Env.UNKNOWN == Env.transformEnv(env)) {
      throw new BadRequestException("env is illegal");
    }

    // 构建名称空间环境分配的角色用户列表
    NamespaceEnvRolesAssignedUsers assignedUsers = new NamespaceEnvRolesAssignedUsers();
    assignedUsers.setNamespaceName(namespaceName);
    assignedUsers.setAppId(appId);
    assignedUsers.setEnv(Env.valueOf(env).toString());

    Set<UserInfo> releaseNamespaceUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName, env));
    assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

    Set<UserInfo> modifyNamespaceUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName, env));
    assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

    return assignedUsers;
  }

  /**
   * 判断指定的应用环境名称空间是否有指定的角色类型权限.
   *
   * @param appId         应用id
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @param user          用户身份标识(用户名)
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @PostMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType}")
  public ResponseEntity<Void> assignNamespaceEnvRoleToUser(@PathVariable String appId,
      @PathVariable String env, @PathVariable String namespaceName,
      @PathVariable String roleType, @RequestBody String user) {
    checkUserExists(user);
    RequestPrecondition.checkArgumentsNotEmpty(user);
    // 验证
    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }
    if (Env.UNKNOWN == Env.transformEnv(env)) {
      throw new BadRequestException("env is illegal");
    }

    // 分配用户角色
    Set<String> assignedUser = rolePermissionService
        .assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType, env),
            Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    if (CollectionUtils.isEmpty(assignedUser)) {
      throw new BadRequestException(user + " already authorized");
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 删除应用环境名称空间的用户角色信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @param user          用户身份标识(用户名)
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @DeleteMapping("/apps/{appId}/envs/{env}/namespaces/{namespaceName}/roles/{roleType}")
  public ResponseEntity<Void> removeNamespaceEnvRoleFromUser(@PathVariable String appId,
      @PathVariable String env, @PathVariable String namespaceName,
      @PathVariable String roleType, @RequestParam String user) {
    RequestPrecondition.checkArgumentsNotEmpty(user);
    // 1.校验
    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }
    // 验证环境参数
    if (Env.UNKNOWN == Env.transformEnv(env)) {
      throw new BadRequestException("env is illegal");
    }

    // 2.删除
    rolePermissionService
        .removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType, env),
            Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 获取应用下名称空间的分配的角色用户.
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 名称空间角色分配的角色用户列表
   */
  @GetMapping("/apps/{appId}/namespaces/{namespaceName}/role_users")
  public NamespaceRolesAssignedUsers getNamespaceRoles(@PathVariable String appId,
      @PathVariable String namespaceName) {

    // 构建名称空间分配的角色用户
    NamespaceRolesAssignedUsers assignedUsers = new NamespaceRolesAssignedUsers();
    assignedUsers.setNamespaceName(namespaceName);
    assignedUsers.setAppId(appId);

    Set<UserInfo> releaseNamespaceUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName));
    assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

    Set<UserInfo> modifyNamespaceUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName));
    assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

    return assignedUsers;
  }

  /**
   * 名称空间角色分配给用户
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @param user          用户身份标识(用户名)
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @PostMapping("/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}")
  public ResponseEntity<Void> assignNamespaceRoleToUser(@PathVariable String appId,
      @PathVariable String namespaceName,
      @PathVariable String roleType, @RequestBody String user) {
    // 1.校验
    checkUserExists(user);
    RequestPrecondition.checkArgumentsNotEmpty(user);

    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }
    // 2.分配用户
    Set<String> assignedUser = rolePermissionService.assignRoleToUsers(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType), Sets.newHashSet(user),
        userInfoHolder.getUser().getUserId());
    if (CollectionUtils.isEmpty(assignedUser)) {
      throw new BadRequestException(user + " already authorized");
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 删除应用名称空间名称下角色类型的指定用户
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param roleType      角色类型
   * @param user          用户身份标识(用户名)
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @DeleteMapping("/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}")
  public ResponseEntity<Void> removeNamespaceRoleFromUser(@PathVariable String appId,
      @PathVariable String namespaceName,
      @PathVariable String roleType, @RequestParam String user) {
    RequestPrecondition.checkArgumentsNotEmpty(user);

    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService
        .removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
            Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 获取指定应用下的用户信息
   *
   * @param appId 应用id
   * @return 应用的管理员用户信息
   */
  @GetMapping("/apps/{appId}/role_users")
  public AppRolesAssignedUsers getAppRoles(@PathVariable String appId) {
    AppRolesAssignedUsers users = new AppRolesAssignedUsers();
    users.setAppId(appId);

    //设置应用下的所有用户
    Set<UserInfo> masterUsers = rolePermissionService
        .queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));
    users.setMasterUsers(masterUsers);

    return users;
  }

  /**
   * 将应用角色分配给用户
   *
   * @param appId    应用id
   * @param roleType 角色类型
   * @param user     用户
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasManageAppMasterPermission(#appId)")
  @PostMapping("/apps/{appId}/roles/{roleType}")
  public ResponseEntity<Void> assignAppRoleToUser(@PathVariable String appId,
      @PathVariable String roleType,
      @RequestBody String user) {
    // 检查
    checkUserExists(user);
    RequestPrecondition.checkArgumentsNotEmpty(user);

    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }

    //对分配好的用户id做检验，验证是否已经授权过
    Set<String> assignedUsers = rolePermissionService.assignRoleToUsers(RoleUtils.buildAppRoleName(
        appId, roleType), Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    if (CollectionUtils.isEmpty(assignedUsers)) {
      throw new BadRequestException(user + " already authorized");
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 删除应用角色
   *
   * @param appId    应用id
   * @param roleType 角色类型
   * @param user     用户信息
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.hasManageAppMasterPermission(#appId)")
  @DeleteMapping("/apps/{appId}/roles/{roleType}")
  public ResponseEntity<Void> removeAppRoleFromUser(@PathVariable String appId,
      @PathVariable String roleType,
      @RequestParam String user) {
    RequestPrecondition.checkArgumentsNotEmpty(user);

    if (!RoleType.isValidRoleType(roleType)) {
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService.removeRoleFromUsers(RoleUtils.buildAppRoleName(appId, roleType),
        Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 检查用户是否存在
   *
   * @param userId 用户身份标识(用户名)
   */
  private void checkUserExists(String userId) {
    if (userService.findByUserId(userId) == null) {
      throw new BadRequestException(String.format("User %s does not exist!", userId));
    }
  }

  /**
   * 添加创建应用的用户角色.
   *
   * @param userIds 用户id列表
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping("/system/role/createApplication")
  public ResponseEntity<Void> addCreateApplicationRoleToUser(@RequestBody List<String> userIds) {

    userIds.forEach(this::checkUserExists);
    rolePermissionService.assignRoleToUsers(SystemRoleManagerService.CREATE_APPLICATION_ROLE_NAME,
        new HashSet<>(userIds), userInfoHolder.getUser().getUserId());

    return ResponseEntity.ok().build();
  }

  /**
   * 通过用户身份标识(用户名)删除角色权限
   *
   * @param userId 用户身份标识(用户名)
   * @return 响应数据
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping("/system/role/createApplication/{userId}")
  public ResponseEntity<Void> deleteCreateApplicationRoleFromUser(
      @PathVariable("userId") String userId) {
    checkUserExists(userId);
    Set<String> userIds = new HashSet<>();
    userIds.add(userId);
    rolePermissionService.removeRoleFromUsers(SystemRoleManagerService.CREATE_APPLICATION_ROLE_NAME,
        userIds, userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 获取创建应用的角色用户列表信息
   *
   * @return 用户身份标识(用户名)列表信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping("/system/role/createApplication")
  public List<String> getCreateApplicationRoleUsers() {
    return rolePermissionService
        .queryUsersWithRole(SystemRoleManagerService.CREATE_APPLICATION_ROLE_NAME)
        .stream().map(UserInfo::getUserId).collect(Collectors.toList());
  }

  /**
   * 通过用户身份标识(用户名)删除创建应用角色
   *
   * @param userId 用户身份标识(用户名)
   * @return 返回是否有创建应用的权限的属性信息
   */
  @GetMapping("/system/role/createApplication/{userId}")
  public JsonObject hasCreateApplicationPermission(@PathVariable String userId) {
    JsonObject rs = new JsonObject();
    rs.addProperty("hasCreateApplicationPermission",
        permissionValidator.hasCreateApplicationPermission(userId));
    return rs;
  }

  /**
   * 添加应用的系统管理员
   *
   * @param appId  应用id
   * @param userId 用户id
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping("/apps/{appId}/system/master/{userId}")
  public ResponseEntity<Void> addManageAppMasterRoleToUser(@PathVariable String appId,
      @PathVariable String userId) {
    // 检查
    checkUserExists(userId);

    // 添加默认的管理员
    roleInitializationService.initManageAppMasterRole(appId, userInfoHolder.getUser().getUserId());
    Set<String> userIds = new HashSet<>();
    userIds.add(userId);

    // 为指定的应用分配用户角色
    rolePermissionService
        .assignRoleToUsers(RoleUtils.buildAppRoleName(appId, PermissionType.MANAGE_APP_MASTER),
            userIds, userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 为应用禁用指定管理员
   *
   * @param appId  应用id
   * @param userId 用户id
   * @return 成功的响应信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping("/apps/{appId}/system/master/{userId}")
  public ResponseEntity<Void> forbidManageAppMaster(@PathVariable String appId,
      @PathVariable String userId) {
    checkUserExists(userId);
    //初始化管理应用的Master角色
    roleInitializationService.initManageAppMasterRole(appId, userInfoHolder.getUser().getUserId());
    Set<String> userIds = new HashSet<>();
    userIds.add(userId);

    // 删除指定指定应用的管理员用户角色
    rolePermissionService
        .removeRoleFromUsers(RoleUtils.buildAppRoleName(appId, PermissionType.MANAGE_APP_MASTER),
            userIds, userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  /**
   * 是否限制只有超级管理员和拥有管理员分配权限的帐号可以修改项目
   *
   * @return 返回json, true,限制,否则,false
   */
  @GetMapping("/system/role/manageAppMaster")
  public JsonObject isManageAppMasterPermissionEnabled() {
    JsonObject rs = new JsonObject();
    rs.addProperty("isManageAppMasterPermissionEnabled",
        systemRoleManagerService.isManageAppMasterPermissionEnabled());
    return rs;
  }
}
