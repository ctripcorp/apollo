package com.ctrip.framework.apollo.openapi.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 消费者角色表.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ConsumerRole")
@SQLDelete(sql = "Update ConsumerRole set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ConsumerRole extends BaseEntity {

  /**
   * 消费者id
   */
  @Column(name = "ConsumerId", nullable = false)
  private long consumerId;
  /**
   * 角色id
   */
  @Column(name = "RoleId", nullable = false)
  private long roleId;
}
