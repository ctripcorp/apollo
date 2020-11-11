package com.ctrip.framework.apollo.biz.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;

/**
 * 应用实例的配置信息.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@Entity
@Table(name = "InstanceConfig")
public class InstanceConfig {

  /**
   * 自增Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * 实例id
   */
  @Column(name = "InstanceId")
  private long instanceId;
  /**
   * 配置的AppId
   */
  @Column(name = "ConfigAppId", nullable = false)
  private String configAppId;
  /**
   * 配置的集群的名称
   */
  @Column(name = "ConfigClusterName", nullable = false)
  private String configClusterName;
  /**
   * 配置的名称空间名称
   */
  @Column(name = "ConfigNamespaceName", nullable = false)
  private String configNamespaceName;
  /**
   * 发布的Key
   */
  @Column(name = "ReleaseKey", nullable = false)
  private String releaseKey;
  /**
   * 配置获取时间
   */
  @Column(name = "ReleaseDeliveryTime", nullable = false)
  private Date releaseDeliveryTime;
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

  /**
   * 更新时更新最后修改时间，更新之前触发
   * <p> @PreUpdate  事件在实体的状态同步到数据库之前触发，此时的数据还没有真实更新到数据库。
   */
  @PreUpdate
  protected void preUpdate() {
    this.dataChangeLastModifiedTime = new Date();
  }
}
