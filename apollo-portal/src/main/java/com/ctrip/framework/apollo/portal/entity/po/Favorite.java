package com.ctrip.framework.apollo.portal.entity.po;

import com.ctrip.framework.apollo.common.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 应用收藏表
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Favorite")
@SQLDelete(sql = "Update Favorite set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Favorite extends BaseEntity {

  /**
   * appId
   */
  @Column(name = "AppId", nullable = false)
  private String appId;
  /**
   * 用户id
   */
  @Column(name = "UserId", nullable = false)
  private String userId;
  /**
   * 收藏顺序
   */
  @Column(name = "Position")
  private long position;
}