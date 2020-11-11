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

/**
 * 使用配置的应用实例.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@Entity
@Table(name = "Instance")
public class Instance {

  /**
   * 自增Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * AppId
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 集群名称
   */
  @Column(name = "ClusterName", nullable = false)
  private String clusterName;
  /**
   * 数据中心
   */
  @Column(name = "DataCenter", nullable = false)
  private String dataCenter;
  /**
   * 实例ip地址
   */
  @Column(name = "Ip", nullable = false)
  private String ip;
  /**
   * 创建时间
   */
  @Column(name = "DataChange_CreatedTime", nullable = false)
  private Date dataChangeCreatedTime;
  /**
   * 最后修改时间
   */
  @Column(name = "DataChange_LastTime")
  private Date dataChangeLastModifiedTime;

  /**
   * 生成修改时间和创建时间
   * <p>@PrePersist 可帮助我们在持久化之前自动填充实体属性。</p>
   */
  @PrePersist
  protected void prePersist() {
    if (this.dataChangeCreatedTime == null) {
      dataChangeCreatedTime = new Date();
    }
    if (this.dataChangeLastModifiedTime == null) {
      dataChangeLastModifiedTime = dataChangeCreatedTime;
    }
  }
}
