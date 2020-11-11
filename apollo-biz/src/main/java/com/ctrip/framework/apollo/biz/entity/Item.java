package com.ctrip.framework.apollo.biz.entity;

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
 * 属性的配置项
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Item")
@SQLDelete(sql = "Update Item set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Item extends BaseEntity {

  /**
   * 名称空间Id
   */
  @Column(name = "NamespaceId", nullable = false)
  private long namespaceId;
  /**
   * 配置项Key
   */
  @Column(name = "key", nullable = false)
  private String key;
  /**
   * 配置项值
   */
  @Column(name = "value")
  @Lob
  private String value;
  /**
   * 备注
   */
  @Column(name = "comment")
  private String comment;
  /**
   * 行号
   */
  @Column(name = "LineNum")
  private Integer lineNum;
}
