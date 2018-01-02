package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.AdminServiceTestConfiguration;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdminServiceTestConfiguration.class)
@WebIntegrationTest(randomPort = true)
public abstract class AbstractControllerTest {

  @Autowired
  private HttpMessageConverters httpMessageConverters;
  
  RestTemplate restTemplate = new TestRestTemplate();

  @PostConstruct
  private void postConstruct() {
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    restTemplate.setMessageConverters(httpMessageConverters.getConverters());
  }

  @Value("${local.server.port}")
  int port;
}
