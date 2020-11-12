package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.biz.utils.ConfigChangeContentBuilder;
import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 提交记录表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Commit")
@SQLDelete(sql = "Update Commit set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Commit extends BaseEntity {

  /**
   * 配置项改变内容集合，{@link ConfigChangeContentBuilder}
   */
  @Lob
  @Column(name = "ChangeSets", nullable = false)
  private String changeSets;
  /**
   * AppId
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 集群的名称
   */
  @Column(name = "ClusterName", nullable = false)
  private String clusterName;
  /**
   * 名称空间的名称
   */
  @Column(name = "NamespaceName", nullable = false)
  private String namespaceName;
  /**
   * 发布说明
   */
  @Column(name = "Comment")
  private String comment;
}
