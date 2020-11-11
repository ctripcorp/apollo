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
 * 发布历史
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ReleaseHistory")
@SQLDelete(sql = "Update ReleaseHistory set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ReleaseHistory extends BaseEntity {

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
   * 命名空间的名称
   */
  @Column(name = "NamespaceName", nullable = false)
  private String namespaceName;
  /**
   * 分支的名称
   */
  @Column(name = "BranchName", nullable = false)
  private String branchName;
  /**
   * 关联的ReleaseId
   */
  @Column(name = "ReleaseId")
  private long releaseId;
  /**
   * 前一次发布的ReleaseId
   */
  @Column(name = "PreviousReleaseId")
  private long previousReleaseId;
  /**
   * 发布类型，0: 普通发布，1: 回滚，2: 灰度发布，3: 灰度规则更新，4: 灰度合并回主分支发布，5: 主分支发布灰度自动发布，6: 主分支回滚灰度自动发布，7: 放弃灰度
   */
  @Column(name = "Operation")
  private int operation;
  /**
   * 发布上下文信息
   */
  @Column(name = "OperationContext", nullable = false)
  private String operationContext;
}
