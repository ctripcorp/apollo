package com.ctrip.framework.apollo.biz.eureka;


import com.ctrip.framework.apollo.biz.config.BizConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Primary
public class ApolloEurekaClientConfig extends EurekaClientConfigBean {

  @Autowired
  private BizConfig bizConfig;

  /**
   * Assert only one zone: defaultZone, but multiple environments.
   */
  public List<String> getEurekaServerServiceUrls(String myZone) {
    List<String> urls = bizConfig.eurekaServiceUrls();
    return CollectionUtils.isEmpty(urls) ? super.getEurekaServerServiceUrls(myZone) : urls;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
