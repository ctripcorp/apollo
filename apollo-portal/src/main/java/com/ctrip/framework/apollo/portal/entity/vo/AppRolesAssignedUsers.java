package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.Set;
import lombok.Data;

/**
 * 应用的管理员用户实体
 */
@Data
public class AppRolesAssignedUsers {

  /**
   * 应用id
   */
  private String appId;
  /**
   * 管理员用户
   */
  private Set<UserInfo> masterUsers;
}
