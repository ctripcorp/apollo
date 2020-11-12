package com.ctrip.framework.apollo.portal.entity.model;

import java.util.Set;
import lombok.Data;

/**
 * 名称空间灰度待删除发布信息 Model
 */
@Data
public class NamespaceGrayDelReleaseModel extends NamespaceReleaseModel implements Verifiable {

  /**
   * 灰度待删除Key列表
   */
  private Set<String> grayDelKeys;
}
