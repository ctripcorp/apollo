package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 名称空间灰度发布信息 Dto
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NamespaceGrayDelReleaseDTO extends NamespaceReleaseDTO {

  /**
   * 灰度发布的key列表
   */
  private Set<String> grayDelKeys;
}
