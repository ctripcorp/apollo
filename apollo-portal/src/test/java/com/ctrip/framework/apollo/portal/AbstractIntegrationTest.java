package com.ctrip.framework.apollo.portal;


import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PortalApplication.class)
@WebIntegrationTest(randomPort = true)
public abstract class AbstractIntegrationTest {

  RestTemplate restTemplate = new TestRestTemplate();
 
  @PostConstruct
  private void postConstruct() {
    System.setProperty("spring.profiles.active", "test");
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
  }

  @Value("${local.server.port}")
  int port;

}
