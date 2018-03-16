package com.ctrip.framework.apollo.spring.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.AbstractSpringIntegrationTest;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ValueMapping;
import com.ctrip.framework.apollo.spring.property.ValueMappingElement;
import com.ctrip.framework.apollo.spring.property.ValueMappingHolder;
import com.ctrip.framework.apollo.spring.property.ValueMappingProperty;
import com.ctrip.framework.apollo.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;


/**
 *
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingProcessorTest extends AbstractSpringIntegrationTest {

  private final static Logger logger = LoggerFactory.getLogger(ValueMappingProcessorTest.class);

  private static final String USER_JSON =
      "{\"userName\":\"zzz@123.com\",\"gender\":\"male\",\"age\":123,\"mobiles\":[\"12345678901\",\"98765432109\"],\"favors\":{\"color\":\"blue\",\"number\":\"13579\"},\"money\":99999.0}";

  private static final String USER_XML="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + 
      "<beans >\n" + 
      "    <bean id=\"user\" class=\"com.ctrip.framework.apollo.spring.processor.ValueMappingProcessorTest$User\">\n" + 
      "        <property name=\"userName\">\n" + 
      "            <value>zzz@123.com</value>\n" + 
      "        </property>\n" + 
      "        <property name=\"gender\">\n" + 
      "            <value>male</value>\n" + 
      "        </property>\n" + 
      "        <property name=\"age\">\n" + 
      "            <value>123</value>\n" + 
      "        </property>\n" + 
      "        <property name=\"mobiles\">\n" + 
      "            <list>\n" + 
      "               <value>12345678901</value>\n" + 
      "               <value>98765432109</value>\n" + 
      "            </list>\n" + 
      "        </property>\n" + 
      "        <property name=\"favors\">\n" + 
      "            <map>\n" + 
      "               <entry key=\"color\" value=\"blue\"/>\n" + 
      "               <entry key=\"number\" value=\"13579\"/>\n" + 
      "            </map>\n" + 
      "        </property>\n" + 
      "        <property name=\"money\">\n" + 
      "            <value>99999.0</value>\n" + 
      "        </property>\n" + 
      "    </bean>\n" + 
      "</beans>";

  private static final String DEFAULT_ARRAY = "[[-1,-2,-3],[9,8,7]]";
  
  private static final int DEFAULT_TIMEOUT = 100;

  private static final String TIMEOUT_KEY = "timeout";

  private static final String USERJSONSTR_KEY = "userJsonStr";

  private static final String USERJSONSTR1_KEY = "userJsonStr1";

  private static final String USERXMLSTR_KEY = "userXmlStr";

  private static final String USERXMLSTR1_KEY = "userXmlStr1";

  private static final Gson gson = new Gson();

  private static boolean logEnable = true;
  
  @SuppressWarnings("unchecked")
  @Test
  public void testProcessorMethod() throws Exception {
    
    ValueMappingProcessor processor = ApolloInjector.getInstance(ValueMappingProcessor.class);

    CustomConfig bean = new CustomConfig();

    // test createValueMappingElements
    List<ValueMappingElement> elemList = processor.createValueMappingElements(bean);
    Map<String, ValueMappingElement> elemMap = new HashMap<>();
    for (ValueMappingElement elem : elemList) {
      elemMap.put(elem.getElement().getName(), elem);
    }

    ValueMappingElement userElem = elemMap.get("user");
    Assert.assertNotNull(userElem);
    Assert.assertTrue(userElem.isField());
    Assert.assertTrue(userElem.isPropertyKeyExplicit());
    Assert.assertFalse(userElem.isMappingMultipleProperties());
    Assert.assertFalse(userElem.isMethod());

    ValueMappingHolder holder = userElem.getFirstHolder();
    Assert.assertEquals(USERJSONSTR_KEY, holder.getPropKey());
    Assert.assertEquals(USER_JSON, holder.getDefaultValue());
    Assert.assertNull(holder.getCollector());
    ValueMappingParser parser = holder.getParser();
    Assert.assertNotNull(parser);

    Field userField = bean.getClass().getDeclaredField("user");
    userField.setAccessible(true);
    Assert.assertEquals(userField.getType(), holder.getType());
    Assert.assertEquals(userField.getGenericType(), holder.getGenericType());

    Config appConfig = mock(Config.class);
    HashSet<String> keys = Sets.newHashSet(TIMEOUT_KEY, USERJSONSTR_KEY, USERJSONSTR1_KEY,
        USERXMLSTR_KEY, USERXMLSTR1_KEY);
    when(appConfig.getPropertyNames()).thenReturn(keys);
    when(appConfig.getProperty(eq(TIMEOUT_KEY), anyString()))
        .thenReturn(String.valueOf(DEFAULT_TIMEOUT));
    when(appConfig.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(USER_JSON);
    when(appConfig.getProperty(eq(USERJSONSTR1_KEY), anyString())).thenReturn(USER_JSON);
    when(appConfig.getProperty(eq(USERXMLSTR_KEY), anyString())).thenReturn(USER_XML);
    when(appConfig.getProperty(eq(USERXMLSTR1_KEY), anyString())).thenReturn(USER_XML);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, appConfig);

    Environment environment = mock(Environment.class);
    when(environment.getProperty(eq(TIMEOUT_KEY), anyString()))
        .thenReturn(String.valueOf(DEFAULT_TIMEOUT));
    when(environment.getProperty(eq(TIMEOUT_KEY)))
    .thenReturn(String.valueOf(DEFAULT_TIMEOUT));
    when(environment.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(USER_JSON);
    when(environment.getProperty(eq(USERJSONSTR_KEY))).thenReturn(USER_JSON);
    when(environment.getProperty(eq(USERJSONSTR1_KEY), anyString())).thenReturn(USER_JSON);
    when(environment.getProperty(eq(USERJSONSTR1_KEY))).thenReturn(USER_JSON);
    when(environment.getProperty(eq(USERXMLSTR_KEY), anyString())).thenReturn(USER_XML);
    when(environment.getProperty(eq(USERXMLSTR_KEY))).thenReturn(USER_XML);
    when(environment.getProperty(eq(USERXMLSTR1_KEY), anyString())).thenReturn(USER_XML);
    when(environment.getProperty(eq(USERXMLSTR1_KEY))).thenReturn(USER_XML);

    // test updateProperty for field
    processor.updateProperty(bean, userElem, environment);

    User user = bean.getUser();
    Assert.assertEquals(USER_JSON, gson.toJson(user));

    ValueMappingElement xmlUserElem = elemMap.get("xmlUser");
    Assert.assertNotNull(xmlUserElem);
    processor.updateProperty(bean, xmlUserElem, environment);
    User xmlUser = bean.getXmlUser();
    Assert.assertEquals(USER_JSON, gson.toJson(xmlUser));

    user.setAge(9999);
    user.getFavors().put("home", "hz");
    final String modUserJson = gson.toJson(user);

    ConfigChange configChange = mock(ConfigChange.class);
    when(configChange.getNewValue()).thenReturn(modUserJson);
    ConfigChangeEvent changeEvent = mock(ConfigChangeEvent.class);
    when(changeEvent.getNamespace()).thenReturn(ConfigConsts.NAMESPACE_APPLICATION);
    when(changeEvent.isChanged(eq(USERJSONSTR_KEY))).thenReturn(true);
    when(changeEvent.changedKeys()).thenReturn(Sets.newHashSet(USERJSONSTR_KEY));
    when(changeEvent.getChange(eq(USERJSONSTR_KEY))).thenReturn(configChange);
    when(environment.getProperty(eq(USERJSONSTR_KEY))).thenReturn(modUserJson);
    when(environment.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(modUserJson);

    // test isPropertyChanged for field
    boolean changed = processor.isPropertyChanged(userElem, changeEvent, environment);
    Assert.assertTrue(changed);

    // test updateProperty for method
    final int modTimeout = DEFAULT_TIMEOUT * 99;
    when(environment.getProperty(eq(TIMEOUT_KEY), anyString()))
        .thenReturn(String.valueOf(modTimeout));
    ValueMappingElement setTimeoutElem = elemMap.get("setTimeout");
    Assert.assertNotNull(setTimeoutElem);
    Object[] args = (Object[]) processor.updateProperty(bean, setTimeoutElem, environment);
    Assert.assertNotNull(args);
    Assert.assertEquals(1, args.length);
    Assert.assertEquals(modTimeout, args[0]);

    ValueMappingElement setUserElem = elemMap.get("setUser");
    Assert.assertNotNull(setUserElem);
    args = (Object[]) processor.updateProperty(bean, setUserElem, environment);
    Assert.assertNotNull(args);
    Assert.assertEquals(1, args.length);
    Assert.assertEquals(modUserJson, gson.toJson(args[0]));
    
    ValueMappingElement setAllElem = elemMap.get("setAll");
    Assert.assertNotNull(setAllElem);
    args = (Object[]) processor.updateProperty(bean, setAllElem, environment);
    Assert.assertNotNull(args);
    Assert.assertEquals(4, args.length);
    Assert.assertEquals(modTimeout, args[0]);
    Assert.assertEquals(modUserJson, gson.toJson(args[1]));
    
    List<User> userList = (List<User>) args[2];
    Assert.assertNotNull(userList);
    Assert.assertEquals(2, userList.size());
    String usrJson1 = gson.toJson(userList.get(0));
    String usrJson2 = gson.toJson(userList.get(1));
    Assert.assertTrue(modUserJson.equals(usrJson1) || USER_JSON.equals(usrJson1));
    Assert.assertTrue(modUserJson.equals(usrJson2) || USER_JSON.equals(usrJson2));
    
    Assert.assertEquals(USER_JSON, gson.toJson(args[3]));

    user.getFavors().put("home", "hzzzz");
    final String modUserJson1 = gson.toJson(user);
    when(configChange.getNewValue()).thenReturn(modUserJson1);
    when(environment.getProperty(eq(USERJSONSTR_KEY))).thenReturn(modUserJson1);
    when(environment.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(modUserJson1);
    // test isPropertyChanged for method
    changed = processor.isPropertyChanged(setAllElem, changeEvent, environment);
    Assert.assertTrue(changed);
  }

  @Test
  public void testConcurrency() {
    // test thread safe
    logEnable = false;
    long time = System.currentTimeMillis();
    ThreadPoolUtils.concurrentExecute(8, 1000, new Runnable() {

      @Override
      public void run() {
        try {
          testProcessorMethod();
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }

    });

    time = System.currentTimeMillis() - time;
    System.out.println("Time: " + time);
  }

  @Test
  public void testApolloConfig() {
    logEnable = true;
    Config appConfig = mock(Config.class);
    HashSet<String> keys = Sets.newHashSet(TIMEOUT_KEY, USERJSONSTR_KEY, USERJSONSTR1_KEY,
        USERXMLSTR_KEY, USERXMLSTR1_KEY);
    when(appConfig.getPropertyNames()).thenReturn(keys);
    when(appConfig.getProperty(eq(TIMEOUT_KEY), anyString()))
        .thenReturn(String.valueOf(DEFAULT_TIMEOUT));
    when(appConfig.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(USER_JSON);
    when(appConfig.getProperty(eq(USERJSONSTR1_KEY), anyString())).thenReturn(USER_JSON);
    when(appConfig.getProperty(eq(USERXMLSTR_KEY), anyString())).thenReturn(USER_XML);
    when(appConfig.getProperty(eq(USERXMLSTR1_KEY), anyString())).thenReturn(USER_XML);
    mockConfig(ConfigConsts.NAMESPACE_APPLICATION, appConfig);

    final List<ConfigChangeListener> listeners = Lists.newArrayList();

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        listeners.add(invocation.getArgumentAt(0, ConfigChangeListener.class));
        return Void.class;
      }
    }).when(appConfig).addChangeListener(any(ConfigChangeListener.class));

    CustomConfig bean = getBean(CustomConfig.class, AppConfig.class);

    Assert.assertEquals(USER_JSON, gson.toJson(bean.getUser()));
    Assert.assertEquals(USER_JSON, gson.toJson(bean.getXmlUser()));
    Assert.assertEquals(DEFAULT_TIMEOUT, CustomConfig.getTimeout());
    //check default value initialization
    Assert.assertEquals(USER_JSON, gson.toJson(bean.getDefaultUser()));
    Assert.assertEquals(DEFAULT_ARRAY, gson.toJson(CustomConfig.getDefaultArray()));
    
    List<User> userList = bean.getXmlUserList();
    Assert.assertNotNull(userList);
    Assert.assertEquals(2, userList.size());
    Assert.assertEquals(USER_JSON, gson.toJson(userList.get(0)));
    Assert.assertEquals(USER_JSON, gson.toJson(userList.get(1)));

    User[] xmlUsers = bean.getXmlUserArray();
    Assert.assertNotNull(xmlUsers);
    Assert.assertEquals(1, xmlUsers.length);
    Assert.assertEquals(USER_JSON, gson.toJson(xmlUsers[0]));

    User[] users = bean.getUserArray();
    Assert.assertNotNull(users);
    Assert.assertEquals(1, users.length);
    Assert.assertEquals(USER_JSON, gson.toJson(users[0]));

    Set<User> userSet = bean.getUserSet();
    Assert.assertNotNull(userSet);
    Assert.assertEquals(2, userSet.size());
    for (User u : userSet) {
      Assert.assertEquals(USER_JSON, gson.toJson(u));
    }

    User modUser = gson.fromJson(gson.toJson(bean.getUser()), User.class);
    modUser.setAge(997799);
    modUser.getFavors().put("home", "zxddh");
    final String modUserJson = gson.toJson(modUser);
    Assert.assertNotEquals(modUserJson, gson.toJson(bean.getUser()));

    ConfigChange configChange = mock(ConfigChange.class);
    when(configChange.getNewValue()).thenReturn(modUserJson);
    ConfigChangeEvent changeEvent = mock(ConfigChangeEvent.class);
    when(changeEvent.getNamespace()).thenReturn(ConfigConsts.NAMESPACE_APPLICATION);
    when(changeEvent.isChanged(eq(USERJSONSTR_KEY))).thenReturn(true);
    when(changeEvent.changedKeys()).thenReturn(Sets.newHashSet(USERJSONSTR_KEY));
    when(changeEvent.getChange(eq(USERJSONSTR_KEY))).thenReturn(configChange);
    when(appConfig.getProperty(eq(USERJSONSTR_KEY), anyString())).thenReturn(modUserJson);

    logger.info("===================================");
    for (ConfigChangeListener listener : listeners) {
      listener.onChange(changeEvent);
    }
    Assert.assertEquals(modUserJson, gson.toJson(bean.getUser()));
    Iterator<User> it = bean.getUserSet().iterator();
    Assert.assertEquals(modUserJson, gson.toJson(it.next()));
    Assert.assertEquals(USER_JSON, gson.toJson(it.next()));
    
  }

  @SuppressWarnings("resource")
  private <T> T getBean(Class<T> beanClass, Class<?>... annotatedClasses) {
      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
          annotatedClasses);
      return context.getBean(beanClass);
  }

  @Configuration
  @EnableApolloConfig(ConfigConsts.NAMESPACE_APPLICATION)
  static class AppConfig {
    @Bean
    public CustomConfig bean() {
      return new CustomConfig();
    }
  }

  @Component
  @EnableApolloConfig(ConfigConsts.NAMESPACE_APPLICATION)
  protected static class CustomConfig {

    @ValueMapping("${timeout:100}")
    private static int timeout;

    @ValueMapping("${userJsonStr:" + USER_JSON + "}")
    private User user;

    @ValueMapping(value = "${userXmlStr:" + USER_XML
        + "}", parser = ValueMappingXmlBeanParser.class)
    private User xmlUser;

    @ValueMapping(value = "${userXml*}", collector = ValueMappingAntMatchCollector.class, parser = ValueMappingXmlBeanParser.class)
    private List<User> xmlUserList;

    @ValueMapping(value = "${userXmlStr[0-9]+}", collector = ValueMappingRegMatchCollector.class, parser = ValueMappingXmlBeanParser.class)
    private User[] xmlUserArray;

    @ValueMapping(value = "${userJs?n*}", collector = CustomAntCollector.class)
    private LinkedHashSet<User> userSet;

    @ValueMapping(value = "${userJsonS[tr]{2}}", collector = ValueMappingRegMatchCollector.class)
    private User[] userArray;

    @ValueMapping("${defaultUserJsonStr:" + USER_JSON + "}")
    private User defaultUser;
    
    private static int[][] defaultArray;
    
    protected int setTimeout(@Value("${timeout:100}") int timeout) {
      if (logEnable) {
        logger.info("[op:setTimeout] timeout={}", timeout);
      }
      return timeout;
    }

    @ValueMapping("${userJsonStr}")
    private void setUser(User user) {
      if (logEnable) {
        logger.info("[op:setUser] user={}", gson.toJson(user));
      }
    }

    protected void setAll(@Value("${timeout:100}") int timeout,
        @ValueMapping("${userJsonStr}") User user,
        @ValueMapping(value = "${userJs?n*}", collector = ValueMappingAntMatchCollector.class) List<User> userList,
        @ValueMapping(value = "${userXmlStr}", parser = ValueMappingXmlBeanParser.class) User xmlUser) {
      if (logEnable) {
        logger.info("[op:setAll] timeout={} user={} xmlUser={} userList={}", timeout,
            gson.toJson(user), gson.toJson(xmlUser), gson.toJson(userList));
        logger.info("[op:setAll] userList={}", gson.toJson(userList));
      }
    }

    @ValueMapping("${defaultArray:" + DEFAULT_ARRAY + "}")
    private static void setDefaultArray(int[][] array) {
      logger.info("[op:setDefaultArray] array={}", gson.toJson(array));
      defaultArray = array;
    }
    
    public static int getTimeout() {
      return timeout;
    }

    public User getUser() {
      return user;
    }

    public User getXmlUser() {
      return xmlUser;
    }

    public List<User> getXmlUserList() {
      return xmlUserList;
    }

    public User[] getXmlUserArray() {
      return xmlUserArray;
    }

    public Set<User> getUserSet() {
      return userSet;
    }

    public User[] getUserArray() {
      return userArray;
    }

    public User getDefaultUser() {
      return defaultUser;
    }

    public static int[][] getDefaultArray() {
      return defaultArray;
    }

  }

  private static final class CustomAntCollector extends ValueMappingAntMatchCollector {

    @Override
    public void postFilter(List<ValueMappingProperty> propList) {
      Collections.sort(propList, new Comparator<ValueMappingProperty>() {

        @Override
        public int compare(ValueMappingProperty o1, ValueMappingProperty o2) {
          return o1.getKey().compareTo(o2.getKey());
        }
      });
    }
  }
  
  protected static class User {

    private String userName;

    private String gender;

    private int age;

    private List<String> mobiles;

    private Map<String, Object> favors;

    private BigDecimal money;

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public String getGender() {
      return gender;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public List<String> getMobiles() {
      return mobiles;
    }

    public void setMobiles(List<String> mobiles) {
      this.mobiles = mobiles;
    }

    public Map<String, Object> getFavors() {
      return favors;
    }

    public void setFavors(Map<String, Object> favors) {
      this.favors = favors;
    }

    public BigDecimal getMoney() {
      return money;
    }

    public void setMoney(BigDecimal money) {
      this.money = money;
    }

  }
}
