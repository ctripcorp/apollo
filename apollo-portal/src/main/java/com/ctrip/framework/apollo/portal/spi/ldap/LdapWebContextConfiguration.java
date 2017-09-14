package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.spi.ldap.filters.UserAccessFilter;
import com.ctrip.framework.apollo.portal.spi.ldap.filters.XxdUserAccessFilter;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapWebContextConfiguration {

    @Bean
    public FilterRegistrationBean userAccessFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean();
        filter.setFilter(new UserAccessFilter());
        filter.addUrlPatterns("/*");
        return filter;
    }

}
