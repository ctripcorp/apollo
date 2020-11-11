package com.ctrip.framework.apollo.portal.entity.vo;

import lombok.Data;

/**
 * 部门信息.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class Organization {

  /**
   * 部门id
   */
  private String orgId;
  /**
   * 部门名称
   */
  private String orgName;
}
