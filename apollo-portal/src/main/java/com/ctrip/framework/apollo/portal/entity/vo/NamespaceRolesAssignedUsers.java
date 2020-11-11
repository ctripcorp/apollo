package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.Set;
import lombok.Data;

/**
 * 名称空间分配的角色用户
 */
@Data
public class NamespaceRolesAssignedUsers {

  /**
   * 应用id
   */
  private String appId;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 修改角色的用户
   */
  private Set<UserInfo> modifyRoleUsers;
  /**
   * 发布角色的用户
   */
  private Set<UserInfo> releaseRoleUsers;
}
