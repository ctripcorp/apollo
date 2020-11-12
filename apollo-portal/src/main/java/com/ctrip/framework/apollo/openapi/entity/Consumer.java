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
 * 开放API消费者
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Consumer")
@SQLDelete(sql = "Update Consumer set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Consumer extends BaseEntity {

  /**
   * 应用名
   */
  @Column(name = "Name", nullable = false)
  private String name;
  /**
   * AppId
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 部门Id
   */
  @Column(name = "OrgId", nullable = false)
  private String orgId;
  /**
   * 部门名字
   */
  @Column(name = "OrgName", nullable = false)
  private String orgName;
  /**
   * 所有者名称
   */
  @Column(name = "OwnerName", nullable = false)
  private String ownerName;
  /**
   * 所有者邮箱
   */
  @Column(name = "OwnerEmail", nullable = false)
  private String ownerEmail;
}