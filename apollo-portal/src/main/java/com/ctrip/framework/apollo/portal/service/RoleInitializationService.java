package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.entity.App;

/**
 * 角色初始化 Service
 */
public interface RoleInitializationService {

  /**
   * 初始化应用角色
   *
   * @param app 应用对象
   */
  void initAppRoles(App app);

  /**
   * 初始化名称空间角色
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   */
  void initNamespaceRoles(String appId, String namespaceName, String operator);

  /**
   * 初始化名称空间环境角色
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param operator      操作者
   */
  void initNamespaceEnvRoles(String appId, String namespaceName, String operator);

  /**
   * 初始化命名空间特定环境的角色
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @param operator      操作者
   */
  void initNamespaceSpecificEnvRoles(String appId, String namespaceName, String env,
      String operator);

  /**
   * 初始化创建的应用的角色
   */
  void initCreateAppRole();

  /**
   * 初始化应用程序管理员角色
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  void initManageAppMasterRole(String appId, String operator);

}
