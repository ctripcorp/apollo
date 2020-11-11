package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import lombok.Data;

/**
 * 配置项 业务对象
 */
@Data
public class ItemBO {

  /**
   * 配置项Dto
   */
  private ItemDTO item;
  /**
   * 是否修改
   */
  private boolean isModified;
  /**
   * 是否删除
   */
  private boolean isDeleted;
  /**
   * 旧值
   */
  private String oldValue;
  /**
   * 新值
   */
  private String newValue;
}
