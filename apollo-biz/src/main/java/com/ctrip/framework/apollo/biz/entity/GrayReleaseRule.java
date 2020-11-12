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
 * 灰度规则 表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GrayReleaseRule")
@SQLDelete(sql = "Update GrayReleaseRule set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class GrayReleaseRule extends BaseEntity {

  /**
   * 应用Id
   */
  @Column(name = "appId", nullable = false)
  private String appId;
  /**
   * 集群的名称
   */
  @Column(name = "ClusterName", nullable = false)
  private String clusterName;
  /**
   * 名称空间名称
   */
  @Column(name = "NamespaceName", nullable = false)
  private String namespaceName;
  /**
   * 分支名称
   */
  @Column(name = "BranchName", nullable = false)
  private String branchName;
  /**
   * 灰度规则
   */
  @Column(name = "Rules")
  private String rules;
  /**
   * 灰度对应的release
   */
  @Column(name = "releaseId", nullable = false)
  private Long releaseId;
  /**
   * 灰度分支状态: 0:删除分支,1:正在使用的规则 2：全量发布
   */
  @Column(name = "BranchStatus", nullable = false)
  private int branchStatus;
}