package com.ctrip.framework.apollo.openapi.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息者审计
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ConsumerAudit")
public class ConsumerAudit {

  /**
   * 自增Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * 消费者Id
   */
  @Column(name = "ConsumerId", nullable = false)
  private long consumerId;
  /**
   * 访问的Uri
   */
  @Column(name = "Uri", nullable = false)
  private String uri;
  /**
   * 访问的Method
   */
  @Column(name = "Method", nullable = false)
  private String method;
  /**
   * 创建时间
   */
  @Column(name = "DataChange_CreatedTime")
  private Date dataChangeCreatedTime;
  /**
   * 最后修改时间
   */
  @Column(name = "DataChange_LastTime")
  private Date dataChangeLastModifiedTime;

  /**
   * 生成最后修改时间和创建时间
   * <p>@PrePersist 可帮助我们在持久化之前自动填充实体属性。</p>
   */
  @PrePersist
  protected void prePersist() {
    if (this.dataChangeCreatedTime == null) {
      this.dataChangeCreatedTime = new Date();
    }
    if (this.dataChangeLastModifiedTime == null) {
      dataChangeLastModifiedTime = this.dataChangeCreatedTime;
    }
  }
}