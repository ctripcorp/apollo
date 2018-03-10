package com.ctrip.framework.apollo.demo.spring.autoRefreshDemo;

import com.ctrip.framework.apollo.demo.spring.common.bean.AnnotatedBean;
import com.ctrip.framework.apollo.demo.spring.common.bean.AutoRefreshBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Scanner;

/**
 * @author tony jiang(258737400@qq.com)
 */
public class AutoRefreshDemo {

  public static void main(String[] args) {
    ApplicationContext appContext =new ClassPathXmlApplicationContext("spring.xml");
    sayHello(appContext);
  }

  private  static void sayHello(ApplicationContext appContext) {
    while(true){
      AutoRefreshBean bean=(AutoRefreshBean)appContext.getBean("autoRefreshBean");
      System.out.println(
              "Please enter the following number to observe the changes in the configuration");
      System.out.println("1.String 2.int 3.long 4.short 5.float 6.double 7.byte 8.boolean");
      String type =new Scanner(System.in).nextLine();
      switch (type) {
        case "1":
          System.out.println("String:"+bean.getSvalue());
          break;
        case "2":
          System.out.println("int:"+bean.getIntValue());
          break;
        case "3":
          System.out.println("long:"+bean.getLongValue());
          break;
        case "4":
          System.out.println("short:"+bean.getShortValue());
          break;
        case "5":
          System.out.println("float:"+bean.getFloatValue());
          break;
        case "6":
          System.out.println("double:"+bean.getDoubleValue());
          break;
        case "7":
          System.out.println("byte:"+bean.getByteValue());
          break;
        case "8":
          System.out.println("boolean:"+bean.isBooleanValue());
          break;
        default:
          break;
      }

    }
  }
}
