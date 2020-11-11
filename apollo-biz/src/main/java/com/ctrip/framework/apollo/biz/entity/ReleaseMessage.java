package com.ctrip.framework.apollo.biz.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发布消息.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@NoArgsConstructor
@Data
@Entity
@Table(name = "ReleaseMessage")
public class ReleaseMessage {

  public ReleaseMessage(String message) {
    this.message = message;
  }

  /**
   * 自增主键
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * 发布消息内容
   */
  @Column(name = "Message", nullable = false)
  private String message;
  /**
   * 最后修改时间
   */
  @Column(name = "DataChange_LastTime")
  private Date dataChangeLastModifiedTime;

  /**
   * 生成最后修改时间
   * <p>@PrePersist 可帮助我们在持久化之前自动填充实体属性。</p>
   */
  @PrePersist
  protected void prePersist() {
    if (this.dataChangeLastModifiedTime == null) {
      dataChangeLastModifiedTime = new Date();
    }
  }
}
