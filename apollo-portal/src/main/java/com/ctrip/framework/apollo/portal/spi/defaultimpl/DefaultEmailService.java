package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.spi.EmailService;

/**
 * 默认的邮件 服务实现类，默认值也不干
 */
public class DefaultEmailService implements EmailService {

  @Override
  public void send(Email email) {
    //do nothing
  }
}
