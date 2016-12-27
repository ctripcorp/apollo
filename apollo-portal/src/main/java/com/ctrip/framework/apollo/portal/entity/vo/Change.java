package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.portal.entity.bo.KVEntity;
import com.ctrip.framework.apollo.portal.enums.ChangeType;

public class Change {

  private ChangeType type;
  private EntityPair<KVEntity> entity;

  public Change(ChangeType type, EntityPair<KVEntity> entity) {
    this.type = type;
    this.entity = entity;
  }

  public ChangeType getType() {
    return type;
  }

  public void setType(ChangeType type) {
    this.type = type;
  }

  public EntityPair<KVEntity> getEntity() {
    return entity;
  }

  public void setEntity(EntityPair<KVEntity> entity) {
    this.entity = entity;
  }
}
