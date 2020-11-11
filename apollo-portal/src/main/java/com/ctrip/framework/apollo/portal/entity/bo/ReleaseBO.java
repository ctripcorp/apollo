package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import java.util.Set;
import lombok.Data;

/**
 * 发布信息 业务对象
 */
@Data
public class ReleaseBO {

  /**
   * 发布信息
   */
  private ReleaseDTO baseInfo;
  /**
   * 配置项列表
   */
  private Set<KVEntity> items;
}
