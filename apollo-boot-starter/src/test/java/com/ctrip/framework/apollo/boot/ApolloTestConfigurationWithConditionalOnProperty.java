package com.ctrip.framework.apollo.boot;

import com.ctrip.framework.apollo.boot.bean.TestBean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApolloTestConfigurationWithConditionalOnProperty {

    @Bean
    @ConditionalOnProperty(prefix = "apollo.test", name = "testBean")
    public TestBean testBean() {
        return new TestBean();
    }
}
