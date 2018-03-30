package com.ctrip.framework.apollo.demo.spring.springBootDemo;

import com.ctrip.framework.apollo.demo.spring.common.bean.AnnotatedBean;
import com.ctrip.framework.apollo.demo.spring.springBootDemo.config.SampleRedisConfig;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@SpringBootApplication(scanBasePackages = {"com.ctrip.framework.apollo.demo.spring.common",
    "com.ctrip.framework.apollo.demo.spring.springBootDemo"
})
public class SpringBootSampleApplication {

  private static final Logger logger = LoggerFactory.getLogger(SpringBootSampleApplication.class);

  public static void main(String[] args) throws IOException {
    ApplicationContext context = new SpringApplicationBuilder(SpringBootSampleApplication.class).run(args);
    AnnotatedBean annotatedBean = context.getBean(AnnotatedBean.class);
    SampleRedisConfig redisConfig = null;
    try {
      redisConfig = context.getBean(SampleRedisConfig.class);
    } catch (NoSuchBeanDefinitionException ex) {
      System.out.println("SampleRedisConfig is null, 'redis.cache.enabled' must have been set to false.");
    }

    System.out.println("SpringBootSampleApplication Demo. Input any key except quit to print the values. Input quit to exit.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (!Strings.isNullOrEmpty(input) && input.trim().equalsIgnoreCase("quit")) {
        System.exit(0);
      }

      if ("log".equals(input)) {
        //key=logging.level.com.ctrip.framework.apollo.demo
        //value=debug/info/warn/error
        logger.debug("logging.level : debug");
        logger.info("logging.level : info");
        logger.warn("logging.level : warn");
        logger.error("logging.level : error");
      }

      System.out.println(annotatedBean.toString());
      if (redisConfig != null) {
        System.out.println(redisConfig.toString());
      }
    }
  }
}
