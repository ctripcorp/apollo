package com.ctrip.framework.apollo.spring.processor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.property.ValueMappingOriginValue;
import com.ctrip.framework.apollo.util.ThreadPoolUtils;
import com.google.gson.Gson;

/**
 *
 * @author hzzhangxuehao (zhangxuehaod@163.com)
 */
public class ValueMappingParserTest {


  @Test
  public void testJsonParser() throws Exception {

    final ValueMappingParser parser = ApolloInjector.getInstance(ValueMappingJsonParser.class);

    User user = new User();
    user.setAge("33");
    user.setGender("female");
    user.setMobiles(Arrays.asList("12345678901", "98765432109"));
    user.setUserName("zhangxuehaod@163.com");
    user.setMoney(new BigDecimal("999999.0"));
    Map<String, Object> favors = new HashMap<>();
    favors.put("color", "blue");
    favors.put("pet", "cat");
    favors.put("number", 12345.0);
    Set<Object> extInfo = new HashSet<>();
    extInfo.add("home:hz");
    extInfo.add("city:hz");
    favors.put("extInfo", extInfo);
    user.setFavors(favors);

    final Gson gson = new Gson();
    final String jsonStr = gson.toJson(user);

    CustomConfig config = new CustomConfig();
    Field field = config.getClass().getDeclaredField("user");
    ValueMappingOriginValue origVal =
        new ValueMappingOriginValue(jsonStr, field.getType(), field.getGenericType());

    // test serialization
    User copy = (User) parser.parse(origVal);
    Assert.assertNotNull(copy);
    Assert.assertEquals(copy.getUserName(), user.getUserName());
    Assert.assertEquals(copy.getGender(), user.getGender());
    Assert.assertEquals(copy.getAge(), user.getAge());
    Assert.assertEquals(gson.toJson(copy), jsonStr);

    // make more complex
    extInfo.addAll(Arrays.asList(copy, (User) parser.parse(origVal)));

    final String newJSonStr = gson.toJson(user);
    Assert.assertNotEquals(newJSonStr, jsonStr);
    final ValueMappingOriginValue newOrigVal =
        new ValueMappingOriginValue(newJSonStr, field.getType(), field.getGenericType());

    // test thread safe
    long time = System.currentTimeMillis();
    ThreadPoolUtils.concurrentExecute(32, 100000, new Runnable() {

      @Override
      public void run() {
        User copy = (User) parser.parse(newOrigVal);
        Assert.assertEquals(gson.toJson(copy), newJSonStr);
      }

    });

    time = System.currentTimeMillis() - time;
    System.out.println("JSON parse time: " + time);
    System.out.println(newJSonStr);
  }

  @Test
  public void testXmlParser() throws Exception {

    final ValueMappingParser parser = ApolloInjector.getInstance(ValueMappingXmlBeanParser.class);

    String userXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + 
        "<beans >\n" + 
        "    <bean id=\"user\" class=\"com.ctrip.framework.apollo.spring.processor.ValueMappingParserTest$User\">\n" + 
        "        <property name=\"userName\">\n" + 
        "            <value>zhangxuehaod@163.com</value>\n" + 
        "        </property>\n" + 
        "        <property name=\"age\">\n" + 
        "            <value>123</value>\n" + 
        "        </property>\n" + 
        "        <property name=\"gender\">\n" + 
        "            <value>male</value>\n" + 
        "        </property>\n" + 
        "        <property name=\"money\">\n" + 
        "            <value>9879778.0</value>\n" + 
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
        "               <entry key=\"extInfo\" value=\"pet:cat\"/>\n" + 
        "            </map>\n" + 
        "        </property>\n" + 
        "    </bean>\n" + 
        "</beans>";
    CustomConfig config = new CustomConfig();
    Field field = config.getClass().getDeclaredField("user");
    final ValueMappingOriginValue origVal =
        new ValueMappingOriginValue(userXmlStr, field.getType(), field.getGenericType());

    // test serialization
    User user = (User) parser.parse(origVal);
    checkXmlUser(user);
    
    // test thread safe
    long time = System.currentTimeMillis();
    ThreadPoolUtils.concurrentExecute(16, 10000, new Runnable() {

      @Override
      public void run() {
        User user = (User) parser.parse(origVal);
        checkXmlUser(user);
      }

    });

    final Gson gson = new Gson();
    time = System.currentTimeMillis() - time;
    System.out.println("XML parse time: " + time);
    System.out.println(gson.toJson(user));
  }

  /**
   * @param user
   */
  private void checkXmlUser(User user) {
    Assert.assertNotNull(user);
    Assert.assertEquals("zhangxuehaod@163.com", user.getUserName());
    Assert.assertEquals("123", user.getAge());
    Assert.assertEquals("male", user.getGender());
    Assert.assertEquals("9879778.0", user.getMoney().toString());
    Assert.assertEquals(Arrays.asList("12345678901", "98765432109"), user.getMobiles());
    Map<String, Object> favors = user.getFavors();
    Assert.assertNotNull(favors);
    Assert.assertEquals(3, favors.size());
    Assert.assertEquals("blue", favors.get("color"));
    Assert.assertEquals("13579", favors.get("number"));
    Assert.assertEquals("pet:cat", favors.get("extInfo"));
  }

  protected static class CustomConfig {

    private User user;

    public User getUser() {
      return user;
    }

    public void setUser(User user) {
      this.user = user;
    }

  }

  protected static class User {

    private String userName;

    private String gender;

    private String age;

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

    public String getAge() {
      return age;
    }

    public void setAge(String age) {
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
