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
 * .
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Privilege")
@SQLDelete(sql = "Update Privilege set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Privilege extends BaseEntity {

  @Column(name = "Name", nullable = false)
  private String name;

  @Column(name = "PrivilType", nullable = false)
  private String privilType;

  @Column(name = "NamespaceId")
  private long namespaceId;
}
