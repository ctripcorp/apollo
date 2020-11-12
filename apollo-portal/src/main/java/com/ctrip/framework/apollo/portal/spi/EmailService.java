package com.ctrip.framework.apollo.portal.spi;

import com.ctrip.framework.apollo.portal.entity.bo.Email;

/**
 * 邮件服务
 */
public interface EmailService {

  /**
   * 发送
   *
   * @param email 邮件信息
   */
  void send(Email email);

}
