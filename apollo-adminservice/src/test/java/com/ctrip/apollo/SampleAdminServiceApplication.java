package com.ctrip.apollo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
public class SampleAdminServiceApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(SampleAdminServiceApplication.class).run(args);
  }
}
