package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 配置发布webHook通知
 *
 * @author HuangSheng
 */
@Slf4j
@Component
public class ConfigReleaseWebhookNotifier {

  private final RestTemplateFactory restTemplateFactory;

  private RestTemplate restTemplate;

  public ConfigReleaseWebhookNotifier(RestTemplateFactory restTemplateFactory) {
    this.restTemplateFactory = restTemplateFactory;
  }

  @PostConstruct
  public void init() {
    // 初始化restTemplate
    restTemplate = restTemplateFactory.getObject();
  }

  /**
   * 通知
   *
   * @param webHookUrls    Webhook的Url地址列表
   * @param env            环境
   * @param releaseHistory 发布历史
   */
  public void notify(String[] webHookUrls, Env env, ReleaseHistoryBO releaseHistory) {
    // 为空直接跳出
    if (webHookUrls == null) {
      return;
    }

    // 组装http调用
    for (String webHookUrl : webHookUrls) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
      HttpEntity entity = new HttpEntity(releaseHistory, headers);
      String url = webHookUrl + "?env={env}";
      try {
        restTemplate.postForObject(url, entity, String.class, env);
      } catch (Exception e) {
        log.error("Notify webHook server failed. webHook server url:{}", env, url, e);
      }
    }
  }
}
