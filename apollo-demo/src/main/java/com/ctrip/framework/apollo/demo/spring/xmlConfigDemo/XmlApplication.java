package com.ctrip.framework.apollo.demo.spring.xmlConfigDemo;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ctrip.framework.apollo.demo.spring.xmlConfigDemo.bean.XmlBean;
import com.google.common.base.Charsets;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlApplication {
  public static void main(String[] args) throws IOException {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
    XmlBean xmlBean = context.getBean(XmlBean.class);

    System.out.println("XmlApplication Demo. Input any key except quit to print the values. Input quit to exit.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (!Strings.isNullOrEmpty(input) && input.trim().equalsIgnoreCase("quit")) {
        System.exit(0);
      }

      System.out.println(xmlBean.toString());
    }
  }
}
