package com.ctrip.framework.apollo.portal.entity.vo;

import lombok.Data;

/**
 * 页面设置
 */
@Data
public class PageSetting {

  /**
   * 页面wiki地址
   */
  private String wikiAddress;
  /**
   * 是否允许项目管理员创建私有namespace
   */
  private boolean canAppAdminCreatePrivateNamespace;
}
