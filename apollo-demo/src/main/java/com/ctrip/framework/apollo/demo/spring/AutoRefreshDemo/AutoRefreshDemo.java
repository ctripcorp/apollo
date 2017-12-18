package com.ctrip.framework.apollo.demo.spring.AutoRefreshDemo;

import com.ctrip.framework.apollo.demo.spring.common.bean.AutoRefreshBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ReflectionUtils;

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
              "Please enter the following parameters to observe the changes in the configuration");
      System.out.println("1.String 2.int 3.long 4.short 5.float 6.double 7.byte 8.boolean 9.array 10.date");
      String type =new Scanner(System.in).nextLine();
      switch (type) {
        case "String":
          System.out.println("String:"+bean.getStringValue());
          break;
        case "int":
          System.out.println("int:"+bean.getIntValue());
          break;
        case "long":
          System.out.println("long:"+bean.getLongValue());
          break;
        case "short":
          System.out.println("short:"+bean.getShortValue());
          break;
        case "float":
          System.out.println("float:"+bean.getFloatValue());
          break;
        case "double":
          System.out.println("double:"+bean.getDoubleValue());
          break;
        case "byte":
          System.out.println("byte:"+bean.getByteValue());
          break;
        case "boolean":
          System.out.println("boolean:"+bean.isBooleanValue());
          break;
        case "array":
          System.out.println("array:");
          for (String t:bean.getArrayValue()) {
            System.out.println(t);
          }
          break;
        case "date":
          System.out.println("date:"+bean.getDateValue());
          break;
        default:
          break;
      }

    }
  }
}
