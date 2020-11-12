package com.ctrip.framework.apollo.portal.entity.model;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifier;
import java.util.List;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * 名称空间同步 Model
 */
@Data
public class NamespaceSyncModel implements Verifiable {

  /**
   * 同步至名称空间的信息
   */
  private List<NamespaceIdentifier> syncToNamespaces;
  /**
   * 同步的配置项列表
   */
  private List<ItemDTO> syncItems;

  @Override
  public boolean isInvalid() {
    if (CollectionUtils.isEmpty(syncToNamespaces) || CollectionUtils.isEmpty(syncItems)) {
      return true;
    }
    for (NamespaceIdentifier namespaceIdentifier : syncToNamespaces) {
      if (namespaceIdentifier.isInvalid()) {
        return true;
      }
    }
    return false;
  }
}
