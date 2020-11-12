package com.ctrip.framework.apollo.portal.entity.model;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import lombok.Data;

/**
 * 命名空间创建 Model
 */
@Data
public class NamespaceCreationModel {

  /**
   * 环境
   */
  private String env;
  /**
   * 名称空间信息
   */
  private NamespaceDTO namespace;
}
