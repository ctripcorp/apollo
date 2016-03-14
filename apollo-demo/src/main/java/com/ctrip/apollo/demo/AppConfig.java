package com.ctrip.apollo.demo;

import com.ctrip.apollo.client.ApolloConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
@ComponentScan(value = "com.ctrip.apollo.demo")
public class AppConfig {
    @Bean
    public ApolloConfig apolloConfig() {
        return new ApolloConfig();
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
