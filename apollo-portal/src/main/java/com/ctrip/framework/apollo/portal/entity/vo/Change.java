package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.portal.entity.bo.KVEntity;
import com.ctrip.framework.apollo.portal.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 变更对象
 */
@AllArgsConstructor
@Data
public class Change {

  /**
   * 变更类型
   */
  private ChangeType type;
  /**
   * 实体
   */
  private EntityPair<KVEntity> entity;
}
