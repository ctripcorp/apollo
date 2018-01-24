package com.ctrip.framework.apollo.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ctrip.framework.apollo.boot.bean.TestBean;

@Configuration
public class ApolloTestConfigurationWithoutConditionalOnProperty {

    @Bean
    public TestBean testBean() {
        return new TestBean();
    }
}
