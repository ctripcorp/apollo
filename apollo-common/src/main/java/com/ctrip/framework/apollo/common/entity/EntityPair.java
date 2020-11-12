package com.ctrip.framework.apollo.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 实体对
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@AllArgsConstructor
public class EntityPair<E> {

  /**
   * 第一个实体
   */
  private E firstEntity;
  /**
   * 第二个实体
   */
  private E secondEntity;
}
