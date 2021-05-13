package com.ctrip.framework.apollo.adminservice;

import com.ctrip.framework.apollo.biz.eureka.ApolloEurekaClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : kl
 * After startup, set FetchRegistry to true, refresh eureka client
 **/
@Configuration
@ConditionalOnProperty(value = {"eureka.client.enabled"}, havingValue = "true", matchIfMissing = true)
public class AdminServerEurekaClientConfigure {

    private static final String EUREKA_CLIENT_BEAN_NAME = "eurekaClient";
    private final ApolloEurekaClientConfig eurekaClientConfig;
    private final AtomicBoolean isRefreshed = new AtomicBoolean(false);
    private final RefreshScope refreshScope;

    public AdminServerEurekaClientConfigure(ApolloEurekaClientConfig eurekaClientConfig, RefreshScope refreshScope) {
        this.eurekaClientConfig = eurekaClientConfig;
        this.refreshScope = refreshScope;
    }

    @EventListener
    public void listenApplicationReadyEvent(ApplicationReadyEvent event) {
        this.refreshEurekaClient();
    }

    private void refreshEurekaClient() {
        if (isRefreshed.compareAndSet(false, true) && !eurekaClientConfig.isFetchRegistry()) {
            eurekaClientConfig.setFetchRegistry(true);
            eurekaClientConfig.setRegisterWithEureka(true);
            refreshScope.refresh(EUREKA_CLIENT_BEAN_NAME);
        }
    }
}
