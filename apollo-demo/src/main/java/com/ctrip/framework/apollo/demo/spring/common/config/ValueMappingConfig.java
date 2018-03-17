package com.ctrip.framework.apollo.demo.spring.common.config;

import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ctrip.framework.apollo.demo.spring.common.bean.ValueMappingUserBean;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.processor.ValueMappingAntMatchCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingRegMatchCollector;
import com.ctrip.framework.apollo.spring.processor.ValueMappingXmlBeanParser;
import com.google.gson.Gson;

/**
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
@Component
@EnableApolloConfig("valueMapping")
public class ValueMappingConfig {

  private final static Logger logger = LoggerFactory.getLogger(ValueMappingConfig.class);

  private final Gson gson = new Gson();

  @ValueMapping("${userJsonStr}")
  private ValueMappingUserBean user;

  @ValueMapping("${userName}")
  private static String userName;

  @ValueMapping(value = "${userJs?n*}", collector = ValueMappingAntMatchCollector.class)
  private Set<ValueMappingUserBean> userSet;

  @ValueMapping(value = "${userJsonS[tr]{2}}", collector = ValueMappingRegMatchCollector.class)
  private ValueMappingUserBean[] userArray;

  @ValueMapping(value = "${userXmlStr}", parser = ValueMappingXmlBeanParser.class)
  private ValueMappingUserBean xmlUser;

  @ValueMapping(value = "${userXml*}", collector = ValueMappingAntMatchCollector.class,
      parser = ValueMappingXmlBeanParser.class)
  private List<ValueMappingUserBean> xmlUserList;

  @ValueMapping(value = "${userXmlStr[0-9]+}", collector = ValueMappingRegMatchCollector.class,
      parser = ValueMappingXmlBeanParser.class)
  private ValueMappingUserBean[] xmlUserArray;

  @PostConstruct
  protected void init() {
    logger.info("[op:init] valueMapping userName={} user={}", userName, gson.toJson(user));
  }

  @ApolloConfigChangeListener("valueMapping")
  public void onChange(ConfigChangeEvent changeEvent) {
    logger.info("[op:onChange] valueMapping userName={} userSet={}", userName,
        gson.toJson(userSet));
  }

  protected static void setUserName(@Value("${userName:abc}") String userName) {
    logger.info("[op:setUserName] userName={}", userName);
  }

  @ValueMapping("${userJsonStr}")
  protected void setUser(ValueMappingUserBean user) {
    logger.info("[op:setUser] user={}", gson.toJson(user));
  }

  protected void setAll(@Value("${userName:abc}") String userName,
      @ValueMapping("${userJsonStr}") ValueMappingUserBean user,
      @ValueMapping(value = "${userJs?n*}",
          collector = ValueMappingAntMatchCollector.class) List<ValueMappingUserBean> userList,
      @ValueMapping(value = "${userXmlStr}",
          parser = ValueMappingXmlBeanParser.class) ValueMappingUserBean xmlUser) {
    logger.info("[op:setAll] userName={} user={} xmlUser={} userList={}", userName,
        gson.toJson(user), gson.toJson(xmlUser), gson.toJson(userList));
  }

  public ValueMappingUserBean getUser() {
    return user;
  }

  public static String getUserName() {
    return userName;
  }

  public Set<ValueMappingUserBean> getUserSet() {
    return userSet;
  }

  public ValueMappingUserBean[] getUserArray() {
    return userArray;
  }

  public ValueMappingUserBean getXmlUser() {
    return xmlUser;
  }

  public List<ValueMappingUserBean> getXmlUserList() {
    return xmlUserList;
  }

  public ValueMappingUserBean[] getXmlUserArray() {
    return xmlUserArray;
  }

}
