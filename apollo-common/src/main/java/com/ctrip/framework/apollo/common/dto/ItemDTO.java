package com.ctrip.framework.apollo.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 属性的配置项
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ItemDTO extends BaseDTO {

  /**
   * 主键ID
   */
  private long id;
  /**
   * 集群NamespaceId
   */
  private Long namespaceId;
  /**
   * 配置项Key
   */
  private String key;
  /**
   * 配置项值
   */
  private String value;
  /**
   * 备注
   */
  private String comment;
  /**
   * 行号
   */
  private Integer lineNum;

  public ItemDTO(String key, String value, String comment, int lineNum) {
    this.key = key;
    this.value = value;
    this.comment = comment;
    this.lineNum = lineNum;
  }
}
