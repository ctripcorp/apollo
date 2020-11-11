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
 * 日志审计表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Audit")
@SQLDelete(sql = "Update Audit set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Audit extends BaseEntity {

  /**
   * 操作类型枚举
   */
  public enum OP {
    /**
     * 插入
     */
    INSERT,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE
  }

  /**
   * 表名
   */
  @Column(name = "EntityName", nullable = false)
  private String entityName;
  /**
   * 记录ID
   */
  @Column(name = "EntityId")
  private Long entityId;
  /**
   * 操作类型
   */
  @Column(name = "OpName", nullable = false)
  private String opName;
  /**
   * 备注
   */
  @Column(name = "Comment")
  private String comment;
}
