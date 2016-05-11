package com.ctrip.apollo;

import com.ctrip.apollo.common.controller.WebMvcConfig;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ComponentScan(excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
    SampleAdminServiceApplication.class, AdminServiceApplication.class, WebMvcConfig.class})})
@EnableAutoConfiguration
public class AdminServiceTestConfiguration {

}
