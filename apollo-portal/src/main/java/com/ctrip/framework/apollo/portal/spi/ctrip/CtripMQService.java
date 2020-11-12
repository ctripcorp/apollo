package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.MQService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.gson.Gson;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Ctrip 队列 Service
 */
public class CtripMQService implements MQService {

  /**
   * 日期格式
   */
  private static final org.apache.commons.lang.time.FastDateFormat
      TIMESTAMP_FORMAT = org.apache.commons.lang.time.FastDateFormat
      .getInstance("yyyy-MM-dd hh:mm:ss");
  /**
   * 配置发布通知至NOC的Topic
   */
  private static final String CONFIG_PUBLISH_NOTIFY_TO_NOC_TOPIC = "ops.noc.record.created";

  private static final Gson GSON = new Gson();

  @Autowired
  private AppService appService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private PortalConfig portalConfig;

  private RestTemplate restTemplate;

  /**
   * 初始化RestTemplate
   */
  @PostConstruct
  public void init() {
    restTemplate = new RestTemplate();

    SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
        .getRequestFactory();
    rf.setReadTimeout(portalConfig.readTimeout());
    rf.setConnectTimeout(portalConfig.connectTimeout());

    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setSupportedMediaTypes(
        Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_OCTET_STREAM));

    restTemplate.setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));

  }

  @Override
  public void sendPublishMsg(Env env, ReleaseHistoryBO releaseHistory) {
    if (releaseHistory == null) {
      return;
    }

    // 发布消息
    PublishMsg msg = buildPublishMsg(env, releaseHistory);

    sendMsg(portalConfig.hermesServerAddress(), CONFIG_PUBLISH_NOTIFY_TO_NOC_TOPIC, msg);
  }

  /**
   * 构建发布消息
   *
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 构建的发布消息
   */
  private PublishMsg buildPublishMsg(Env env, ReleaseHistoryBO releaseHistory) {

    // 构建发布消息
    PublishMsg msg = new PublishMsg();

    msg.setPriority("中");
    msg.setTool_origin("Apollo");

    String appId = releaseHistory.getAppId();
    App app = appService.load(appId);
    msg.setInfluence_bu(app.getOrgName());
    msg.setAppid(appId);
    msg.setAssginee(releaseHistory.getOperator());
    msg.setOperation_time(TIMESTAMP_FORMAT.format(releaseHistory.getReleaseTime()));
    msg.setDesc(GSON.toJson(releaseService.compare(env, releaseHistory.getPreviousReleaseId(),
        releaseHistory.getReleaseId())));

    return msg;
  }

  /**
   * 发送消息
   *
   * @param serverAddress 服务地址
   * @param topic         队列的topic
   * @param msg           消息对象
   */
  private void sendMsg(String serverAddress, String topic, Object msg) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM + ";charset=UTF-8"));
    HttpEntity<Object> request = new HttpEntity<>(msg, headers);

    try {
      // 通过restApi 发送消息
      restTemplate.postForObject(serverAddress + "/topics/" + topic, request, Object.class);

    } catch (Exception e) {
      Tracer.logError("Send publish msg to hermes failed", e);
    }

  }

  /**
   * 发布消息对象
   */
  @Data
  private static class PublishMsg {

    /**
     * 分配人
     */
    private String assginee;
    /**
     * 描述
     */
    private String desc;
    /**
     * 操作时间
     */
    private String operation_time;
    /**
     * 工具源
     */
    private String tool_origin;
    /**
     * 优先级
     */
    private String priority;
    /**
     * 部门名字
     */
    private String influence_bu;
    /**
     * 应用id
     */
    private String appid;

  }

}
