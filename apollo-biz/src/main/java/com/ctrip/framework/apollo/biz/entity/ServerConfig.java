package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置服务自身配置
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ServerConfig")
@SQLDelete(sql = "Update ServerConfig set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ServerConfig extends BaseEntity {

  /**
   * 配置项Key
   */
  @Column(name = "Key", nullable = false)
  private String key;
  /**
   * 配置对应的集群，default为不针对特定的集群
   */
  @Column(name = "Cluster", nullable = false)
  private String cluster;
  /**
   * 配置项值
   */
  @Column(name = "Value", nullable = false)
  private String value;
  /**
   * 注释
   */
  @Column(name = "Comment", nullable = false)
  private String comment;
}
