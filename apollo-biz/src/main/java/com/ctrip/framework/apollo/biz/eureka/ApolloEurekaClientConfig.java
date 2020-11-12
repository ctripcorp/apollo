package com.ctrip.framework.apollo.biz.eureka;


import com.ctrip.framework.apollo.biz.config.BizConfig;
import java.util.List;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * apollo Eureka客户端配置类
 */
@Component
@Primary
public class ApolloEurekaClientConfig extends EurekaClientConfigBean {

  private final BizConfig bizConfig;

  public ApolloEurekaClientConfig(final BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  /**
   * 获取Eureka服务Url,如果为空，使用父类的实现
   */
  @Override
  public List<String> getEurekaServerServiceUrls(String myZone) {
    List<String> urls = bizConfig.eurekaServiceUrls();
    return CollectionUtils.isEmpty(urls) ? super.getEurekaServerServiceUrls(myZone) : urls;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
