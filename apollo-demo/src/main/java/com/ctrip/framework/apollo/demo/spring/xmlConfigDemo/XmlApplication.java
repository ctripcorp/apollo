package com.ctrip.framework.apollo.demo.spring.xmlConfigDemo;

import com.ctrip.framework.apollo.demo.spring.xmlConfigDemo.bean.XmlBean;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlApplication {
  public static void main(String[] args) throws IOException {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
    XmlBean xmlBean = context.getBean(XmlBean.class);
    XmlBean xmlBeanNew = null;
    System.out.println("XmlApplication Demo. Input any key except quit to print the values. Input quit to exit.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (!Strings.isNullOrEmpty(input) && input.trim().equalsIgnoreCase("quit")) {
        System.exit(0);
      }

      System.out.println("【init】>> xmlBean.hashCode:"+ xmlBean.hashCode()+ xmlBean.toString());

      if("new".equalsIgnoreCase(input)) {
        xmlBeanNew = context.getBean(XmlBean.class);
        System.out.println("【new】 >> xmlBeanNew.hashCode:"+ xmlBeanNew.hashCode() + xmlBeanNew.toString());
      } else if("sec".equalsIgnoreCase(input)) {
        System.out.println("【sec】 >> xmlBeanNew.hashCode:"+ xmlBeanNew.hashCode() + xmlBeanNew.toString());
      }
    }
  }
}
