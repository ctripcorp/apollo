package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * restTemplate工厂
 */
@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {

  /**
   * http消息转换类
   */
  @Autowired
  private HttpMessageConverters httpMessageConverters;
  @Autowired
  private PortalConfig portalConfig;

  private RestTemplate restTemplate;

  /**
   * 获取对象实例
   *
   * @return RestTemplate对象实例
   */
  @Override
  public RestTemplate getObject() {
    return restTemplate;
  }

  /**
   * 获取Bean的类型
   *
   * @return Bean的类型
   */
  @Override
  public Class<RestTemplate> getObjectType() {
    return RestTemplate.class;
  }

  /**
   * 是否单例
   *
   * @return true是单例，false是非单例
   */
  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    //设置restTemplate的属性
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    restTemplate = new RestTemplate(httpMessageConverters.getConverters());
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(portalConfig.connectTimeout());
    requestFactory.setReadTimeout(portalConfig.readTimeout());

    restTemplate.setRequestFactory(requestFactory);
  }


}
