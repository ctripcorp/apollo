package com.ctrip.framework.apollo.openapi.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 消息者授权token
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ConsumerToken")
@SQLDelete(sql = "Update ConsumerToken set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ConsumerToken extends BaseEntity {

  /**
   * 消费者id
   */
  @Column(name = "ConsumerId", nullable = false)
  private long consumerId;
  /**
   * token
   */
  @Column(name = "token", nullable = false)
  private String token;
  /**
   * token失效时间
   */
  @Column(name = "Expires", nullable = false)
  private Date expires;
}