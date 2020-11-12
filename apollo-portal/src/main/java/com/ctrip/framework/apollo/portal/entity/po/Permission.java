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
 * 权限表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Permission")
@SQLDelete(sql = "Update Permission set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Permission extends BaseEntity {

  /**
   * 权限类型
   */
  @Column(name = "PermissionType", nullable = false)
  private String permissionType;
  /**
   * 权限对象类型
   */
  @Column(name = "TargetId", nullable = false)
  private String targetId;
}