package com.ctrip.framework.apollo.openapi.dto;

import java.util.Date;
import lombok.Data;

/**
 * Dto基类
 */
@Data
public class BaseDTO {

  /**
   * 创建者
   */
  protected String dataChangeCreatedBy;
  /**
   * 最后修改者
   */
  protected String dataChangeLastModifiedBy;
  /**
   * 创建时间
   */
  protected Date dataChangeCreatedTime;
  /**
   * 最后修改时间
   */
  protected Date dataChangeLastModifiedTime;
}
