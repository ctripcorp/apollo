package com.ctrip.framework.apollo.portal.entity.bo;

import java.util.List;
import lombok.Data;

/**
 * 邮件信息
 */
@Data
public class Email {

  /**
   * 发送者邮箱地址
   */
  private String senderEmailAddress;
  /**
   * 收件人列表
   */
  private List<String> recipients;
  /**
   * 主题
   */
  private String subject;
  /**
   * 内容体
   */
  private String body;
}