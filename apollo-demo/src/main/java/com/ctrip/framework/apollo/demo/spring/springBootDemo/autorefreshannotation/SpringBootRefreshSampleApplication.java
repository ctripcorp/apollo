package com.ctrip.framework.apollo.demo.spring.springBootDemo.autorefreshannotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ctrip.framework.apollo.spring.boot.RefreshConfigurationProperties;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@SpringBootApplication
public class SpringBootRefreshSampleApplication {

  public static void main(String[] args) throws IOException {
    ApplicationContext context = new SpringApplicationBuilder(SpringBootRefreshSampleApplication.class).run(args);
    RefreshEnabledRedisConfig refreshEnabledRedisConfig = context.getBean(RefreshEnabledRedisConfig.class);
    RefreshEnabledFiledRedisConfig refreshEnabledFiledRedisConfig = context.getBean(RefreshEnabledFiledRedisConfig.class);
    RefreshDisabledFiledRedisConfig refreshDisabledFiledRedisConfig = context.getBean(RefreshDisabledFiledRedisConfig.class);
    RefreshDisabledBean refreshDisabledBean = context.getBean(RefreshDisabledBean.class);
    RefreshEnabledBean refreshEnabledBean = context.getBean(RefreshEnabledBean.class);
    
    AutoRefreshEnabledFiledRedisConfig autorefreshEnabledFiledRedisConfig = context.getBean(AutoRefreshEnabledFiledRedisConfig.class);
    AutoRefreshDisabledFiledRedisConfig autorefreshDisabledFiledRedisConfig = context.getBean(AutoRefreshDisabledFiledRedisConfig.class);
    AutoRefreshDisabledBean autorefreshDisabledBean = context.getBean(AutoRefreshDisabledBean.class);
    AutoRefreshEnabledBean autorefreshEnabledBean = context.getBean(AutoRefreshEnabledBean.class);
   
    System.out.println("SpringBootSampleApplication Demo. Input any key except quit to print the values. Input quit to exit.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (!Strings.isNullOrEmpty(input) && input.trim().equalsIgnoreCase("quit")) {
        System.exit(0);
      }

      switch (input) {
        case "1":
          System.out.println(refreshEnabledRedisConfig);
          break;
        case "2":
          System.out.println(refreshEnabledFiledRedisConfig);
          break;
        case "3":
          System.out.println(refreshDisabledFiledRedisConfig);
          break;
        case "4":
          System.out.println(refreshDisabledBean);
          break;
        case "5":
          System.out.println(refreshEnabledBean);
          break;
        case "6":
          System.out.println(autorefreshEnabledFiledRedisConfig);
          break;
        case "7":
          System.out.println(autorefreshDisabledFiledRedisConfig);
          break;
        case "8":
          System.out.println(autorefreshDisabledBean);
          break;
        case "9":
          System.out.println(autorefreshEnabledBean);
          break;
        default:
          break;
      }
    }
  }
  
}
