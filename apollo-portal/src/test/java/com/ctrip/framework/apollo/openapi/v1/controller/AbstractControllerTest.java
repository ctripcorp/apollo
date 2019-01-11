package com.ctrip.framework.apollo.openapi.v1.controller;

import javax.annotation.PostConstruct;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Created by kezhenxu at 2019/1/8 18:19.
 *
 * @author kezhenxu (kezhenxu94@163.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractControllerTest {
  @Autowired
  private HttpMessageConverters httpMessageConverters;

  protected RestTemplate restTemplate = (new TestRestTemplate()).getRestTemplate();

  @PostConstruct
  private void postConstruct() {
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    restTemplate.setMessageConverters(httpMessageConverters.getConverters());
  }

  @Value("${local.server.port}")
  int port;

  protected String url(String path) {
    return "http://localhost:" + port + path;
  }
}
