package com.ctrip.apollo.portal.entity;

import com.ctrip.apollo.core.dto.ItemChangeSets;

public class ItemDiffs {
  private NamespaceIdentifer namespace;
  private ItemChangeSets diffs;

  public ItemDiffs(NamespaceIdentifer namespace){
    this.namespace = namespace;
  }
  public NamespaceIdentifer getNamespace() {
    return namespace;
  }

  public void setNamespace(NamespaceIdentifer namespace) {
    this.namespace = namespace;
  }

  public ItemChangeSets getDiffs() {
    return diffs;
  }

  public void setDiffs(ItemChangeSets diffs) {
    this.diffs = diffs;
  }
}
