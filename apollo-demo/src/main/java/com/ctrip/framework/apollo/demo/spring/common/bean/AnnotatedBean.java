package com.ctrip.framework.apollo.demo.spring.common.bean;

import com.ctrip.framework.apollo.spring.annotation.ApolloJSONValue;
import com.google.gson.Gson;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RefreshScope
@Component("annotatedBean")
public class AnnotatedBean {
  private static final Logger logger = LoggerFactory.getLogger(AnnotatedBean.class);

  @Value("${timeout:200}")
  private int timeout;
  private int batch;

  @ApolloJSONValue("${objectList}")
  private List<JsonBean> jsonBeans;

  @PostConstruct
  void initialize() {
    logger.info("timeout is initialized as {}", timeout);
    logger.info("batch is initialized as {}", batch);
    logger.info("jsonBeans is initialized as {}", new Gson().toJson(jsonBeans));
  }

  @Value("${batch:100}")
  public void setBatch(int batch) {
    this.batch = batch;
  }


  @Override
  public String toString() {
    return String.format("[AnnotatedBean] timeout: %d, batch: %d, jsonBeans: %s", timeout, batch, new Gson().toJson(jsonBeans));
  }

  static class JsonBean{
    String a;
    int b;

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    public int getB() {
      return b;
    }

    public void setB(int b) {
      this.b = b;
    }
  }


}
