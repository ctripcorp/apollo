package com.ctrip.framework.apollo.common.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 使用配置的应用实例.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
public class InstanceDTO {

  /**
   * 主键id
   */
  private Long id;
  /**
   * AppId
   */
  private String appId;
  /**
   * 集群的名称
   */
  private String clusterName;
  /**
   * 数据中心
   */
  private String dataCenter;
  /**
   * 实例ip地址
   */
  private String ip;
  /**
   * 应用实例配置
   */
  private List<InstanceConfigDTO> configs;
  /**
   * 创建时间
   */
  private Date dataChangeCreatedTime;
}
