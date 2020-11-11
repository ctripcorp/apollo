package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import java.util.List;
import lombok.Data;

/**
 * 名称空间 业务对象
 */
@Data
public class NamespaceBO {

  /**
   * 名称空间 Dto
   */
  private NamespaceDTO baseInfo;
  /**
   * 配置项修改的次数
   */
  private int itemModifiedCnt;
  /**
   * 配置项内容
   */
  private List<ItemBO> items;
  /**
   * 名称空间的格式（后缀）类型
   */
  private String format;
  /**
   * 名称空间是否为公共
   */
  private boolean isPublic;
  /**
   * 父应用id
   */
  private String parentAppId;
  /**
   * 备注
   */
  private String comment;
  /**
   * 配置是否对当前用户隐藏
   */
  private boolean isConfigHidden;

  /**
   * 隐藏配置项信息
   */
  public void hideItems() {
    setConfigHidden(true);
    items.clear();
    setItemModifiedCnt(0);
  }
}
