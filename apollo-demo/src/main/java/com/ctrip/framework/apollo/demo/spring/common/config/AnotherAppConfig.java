package com.ctrip.framework.apollo.demo.spring.common.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

import org.springframework.context.annotation.Configuration;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
@EnableApolloConfig(value = "TEST1.apollo", order = 11)
public class AnotherAppConfig {
}
