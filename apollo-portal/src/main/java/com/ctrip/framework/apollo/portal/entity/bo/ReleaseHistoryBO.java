package com.ctrip.framework.apollo.portal.entity.bo;

import com.ctrip.framework.apollo.common.entity.EntityPair;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 发布历史信息 业务对象
 */
@Data
public class ReleaseHistoryBO {

  /**
   * 主键
   */
  private Long id;
  /**
   * 应用id
   */
  private String appId;
  /**
   * 集群名称
   */
  private String clusterName;
  /**
   * 名称空间名称
   */
  private String namespaceName;
  /**
   * 分支名称
   */
  private String branchName;
  /**
   * 操作者
   */
  private String operator;
  /**
   * 发布id
   */
  private long releaseId;
  /**
   * 发布标题
   */
  private String releaseTitle;
  /**
   * 发布备注
   */
  private String releaseComment;
  /**
   * 发布时间
   */
  private Date releaseTime;
  /**
   * 发布时间格式
   */
  private String releaseTimeFormatted;
  /**
   * 配置信息
   */
  private List<EntityPair<String>> configuration;
  /**
   * 是否放弃发布
   */
  private boolean isReleaseAbandoned;
  /**
   * 前一个的发布id
   */
  private long previousReleaseId;
  /**
   * 操作类型值
   */
  private int operation;
  /**
   * 操作类型内容
   */
  private Map<String, Object> operationContext;
}
