package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@Table(name = "Permission")
@SQLDelete(sql = "Update Permission set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Permission extends BaseEntity {
  @Column(name = "PermissionType", nullable = false)
  private String permissionType;

  @Column(name = "TargetId", nullable = false)
  private String targetId;

  public String getPermissionType() {
    return permissionType;
  }

  public void setPermissionType(String permissionType) {
    this.permissionType = permissionType;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}
