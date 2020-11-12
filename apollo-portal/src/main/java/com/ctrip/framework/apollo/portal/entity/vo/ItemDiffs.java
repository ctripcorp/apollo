package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import lombok.Data;

/**
 * 配置项差异
 */
@Data
public class ItemDiffs {

  /**
   * 名称空间标识
   */
  private NamespaceIdentifier namespace;
  /**
   * 配置项变更的列表数据
   */
  private ItemChangeSets diffs;
  /**
   * 外部信息
   */
  private String extInfo;

  public ItemDiffs(NamespaceIdentifier namespace) {
    this.namespace = namespace;
  }
}
