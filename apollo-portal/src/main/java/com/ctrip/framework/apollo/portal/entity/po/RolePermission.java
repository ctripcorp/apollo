package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 角色和权限的绑定表.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "RolePermission")
@SQLDelete(sql = "Update RolePermission set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class RolePermission extends BaseEntity {

  /**
   * 角色权限.
   */
  @Column(name = "RoleId", nullable = false)
  private long roleId;
  /**
   * 权限id
   */
  @Column(name = "PermissionId", nullable = false)
  private long permissionId;
}
