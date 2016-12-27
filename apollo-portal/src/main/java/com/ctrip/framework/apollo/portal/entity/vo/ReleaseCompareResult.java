package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.portal.entity.bo.KVEntity;
import com.ctrip.framework.apollo.portal.enums.ChangeType;

import java.util.LinkedList;
import java.util.List;

public class ReleaseCompareResult {

  private List<Change> changes = new LinkedList<>();

  public void addEntityPair(ChangeType type, KVEntity firstEntity, KVEntity secondEntity) {
    changes.add(new Change(type, new EntityPair<>(firstEntity, secondEntity)));
  }

  public boolean hasContent(){
    return !changes.isEmpty();
  }

  public List<Change> getChanges() {
    return changes;
  }

  public void setChanges(List<Change> changes) {
    this.changes = changes;
  }

}
