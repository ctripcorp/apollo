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
 * 用户角色
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "UserRole")
@SQLDelete(sql = "Update UserRole set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class UserRole extends BaseEntity {

  /**
   * 用户身份标识(用户名)
   */
  @Column(name = "UserId", nullable = false)
  private String userId;
  /**
   * 角色id
   */
  @Column(name = "RoleId", nullable = false)
  private long roleId;
}