package com.ctrip.framework.apollo.common.dto;

import java.util.Date;
import lombok.Data;

/**
 * 应用实例配置 Dto
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class InstanceConfigDTO {

  /**
   * 发布信息
   */
  private ReleaseDTO release;
  /**
   * 创建时间
   */
  private Date releaseDeliveryTime;
  /**
   * 最后修改时间
   */
  private Date dataChangeLastModifiedTime;
}
