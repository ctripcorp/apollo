package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.lang.reflect.Method;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ctrip邮件服务
 */
public class CtripEmailService implements EmailService {

  private static final Logger logger = LoggerFactory.getLogger(CtripEmailService.class);

  private Object emailServiceClient;
  private Method sendEmailAsync;
  private Method sendEmail;

  @Autowired
  private CtripEmailRequestBuilder emailRequestBuilder;
  @Autowired
  private PortalConfig portalConfig;

  /**
   * 初始化
   */
  @PostConstruct
  public void init() {
    try {
      initServiceClientConfig();

      Class emailServiceClientClazz =
          Class.forName("com.ctrip.framework.apolloctripservice.emailservice.EmailServiceClient");

      Method getInstanceMethod = emailServiceClientClazz.getMethod("getInstance");
      emailServiceClient = getInstanceMethod.invoke(null);

      Class sendEmailRequestClazz =
          Class.forName("com.ctrip.framework.apolloctripservice.emailservice.SendEmailRequest");
      sendEmailAsync = emailServiceClientClazz.getMethod("sendEmailAsync", sendEmailRequestClazz);
      sendEmail = emailServiceClientClazz.getMethod("sendEmail", sendEmailRequestClazz);
    } catch (Throwable e) {
      logger.error("init ctrip email service failed", e);
      Tracer.logError("init ctrip email service failed", e);
    }
  }

  /**
   * 初始化服务客户端配置
   *
   * @throws Exception
   */
  private void initServiceClientConfig() throws Exception {

    Class serviceClientConfigClazz = Class
        .forName("com.ctriposs.baiji.rpc.client.ServiceClientConfig");
    Object serviceClientConfig = serviceClientConfigClazz.newInstance();
    Method setFxConfigServiceUrlMethod = serviceClientConfigClazz
        .getMethod("setFxConfigServiceUrl", String.class);

    setFxConfigServiceUrlMethod.invoke(serviceClientConfig, portalConfig.soaServerAddress());

    Class serviceClientBaseClazz = Class.forName("com.ctriposs.baiji.rpc.client.ServiceClientBase");
    Method initializeMethod = serviceClientBaseClazz
        .getMethod("initialize", serviceClientConfigClazz);
    initializeMethod.invoke(null, serviceClientConfig);
  }

  @Override
  public void send(Email email) {

    try {
      // l构建请求对象
      Object emailRequest = emailRequestBuilder.buildEmailRequest(email);

      // 异步还是同步发送
      Object sendResponse = portalConfig.isSendEmailAsync() ?
          sendEmailAsync.invoke(emailServiceClient, emailRequest) :
          sendEmail.invoke(emailServiceClient, emailRequest);

      logger.info("Email server response: " + sendResponse);

    } catch (Throwable e) {
      logger.error("send email failed", e);
      Tracer.logError("send email failed", e);
    }


  }

}
