package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.openapi.filter.ConsumerAuthenticationFilter;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuditUtil;
import com.ctrip.framework.apollo.openapi.util.ConsumerAuthUtil;
import com.ctrip.framework.apollo.portal.filter.EnvironmentFilter;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthFilterConfiguration {

    @Bean
    public FilterRegistrationBean openApiAuthenticationFilter(ConsumerAuthUtil consumerAuthUtil,
        ConsumerAuditUtil consumerAuditUtil) {
        FilterRegistrationBean openApiFilter = new FilterRegistrationBean();

        openApiFilter.setFilter(new ConsumerAuthenticationFilter(consumerAuthUtil, consumerAuditUtil));
        openApiFilter.addUrlPatterns("/openapi/*");

        return openApiFilter;
    }

    @Bean
    public FilterRegistrationBean environmentFilter() {

        FilterRegistrationBean environmentFilter = new FilterRegistrationBean();

        environmentFilter.setFilter(new EnvironmentFilter());
        environmentFilter.addUrlPatterns("/*");

        return environmentFilter;
    }


}
