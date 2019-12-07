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
@Table(name = "RolePermission")
@SQLDelete(sql = "Update RolePermission set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class RolePermission extends BaseEntity {
  @Column(name = "RoleId", nullable = false)
  private long roleId;

  @Column(name = "PermissionId", nullable = false)
  private long permissionId;

  public long getRoleId() {
    return roleId;
  }

  public void setRoleId(long roleId) {
    this.roleId = roleId;
  }

  public long getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(long permissionId) {
    this.permissionId = permissionId;
  }
}
