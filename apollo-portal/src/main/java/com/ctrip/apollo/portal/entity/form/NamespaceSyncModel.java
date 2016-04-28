package com.ctrip.apollo.portal.entity.form;

import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.portal.entity.NamespaceIdentifer;

import org.springframework.util.CollectionUtils;

import java.util.List;

public class NamespaceSyncModel implements Verifiable {

  private List<NamespaceIdentifer> syncToNamespaces;
  private List<ItemDTO> syncItems;

  @Override
  public boolean isInvalid() {
    if (CollectionUtils.isEmpty(syncToNamespaces) || CollectionUtils.isEmpty(syncItems)){
      return true;
    }
    for (NamespaceIdentifer namespaceIdentifer: syncToNamespaces){
      if (namespaceIdentifer.isInvalid()){
        return true;
      }
    }
    return false;
  }

  public List<NamespaceIdentifer> getSyncToNamespaces() {
    return syncToNamespaces;
  }

  public void setSyncToNamespaces(List<NamespaceIdentifer> syncToNamespaces) {
    this.syncToNamespaces = syncToNamespaces;
  }

  public List<ItemDTO> getSyncItems() {
    return syncItems;
  }

  public void setSyncItems(List<ItemDTO> syncItems) {
    this.syncItems = syncItems;
  }
}
