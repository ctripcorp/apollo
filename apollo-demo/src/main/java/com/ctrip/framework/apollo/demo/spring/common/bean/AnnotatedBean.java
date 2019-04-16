package com.ctrip.framework.apollo.demo.spring.common.bean;

import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component("annotatedBean")
@Scope("prototype")
public class AnnotatedBean {
  private static final Logger logger = LoggerFactory.getLogger(AnnotatedBean.class);

  private int timeout;
  private int batch;
  private String databaseUrl;
  private List<JsonBean> jsonBeans;

  /**
   * ApolloJsonValue annotated on fields example, the default value is specified as empty list - []
   * <br />
   * jsonBeanProperty=[{"someString":"hello","someInt":100},{"someString":"world!","someInt":200}]
   */
  @ApolloJsonValue("${jsonBeanProperty:[]}")
  private List<JsonBean> anotherJsonBeans;

  @Value("${batch:100}")
  public void setBatch(int batch) {
    logger.info("updating batch, old value: {}, new value: {}", this.batch, batch);
    this.batch = batch;
  }

  @Value("${timeout:200}")
  public void setTimeout(int timeout) {
    logger.info("updating timeout, old value: {}, new value: {}", this.timeout, timeout);
    this.timeout = timeout;
  }

  @Value("${database.url:empty}")
  public void setDatabaseUrl(String databaseUrl) {
    logger.info("updating databaseUrl, old value: {}, new value: {}", this.databaseUrl, databaseUrl);
    this.databaseUrl = databaseUrl;
  }

  /**
   * ApolloJsonValue annotated on methods example, the default value is specified as empty list - []
   * <br />
   * jsonBeanProperty=[{"someString":"hello","someInt":100},{"someString":"world!","someInt":200}]
   */
  @ApolloJsonValue("${jsonBeanProperty:[]}")
  public void setJsonBeans(List<JsonBean> jsonBeans) {
    logger.info("updating json beans, old value: {}, new value: {}", this.jsonBeans, jsonBeans);
    this.jsonBeans = jsonBeans;
  }

  @Override
  public String toString() {
    return String.format("[AnnotatedBean] timeout: %d, batch: %d, jsonBeans: %s, databaseUrl: %s", timeout, batch, jsonBeans, databaseUrl);
  }

  private static class JsonBean{

    private String someString;
    private int someInt;

    @Override
    public String toString() {
      return "JsonBean{" +
          "someString='" + someString + '\'' +
          ", someInt=" + someInt +
          '}';
    }
  }
}
