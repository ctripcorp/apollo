package com.ctrip.framework.apollo.bean;

import com.ctrip.framework.apollo.spring.annotation.RefreshEnabled;
import com.ctrip.framework.apollo.spring.annotation.RefreshField;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("test")
@RefreshEnabled
public class TestConfigurationPropertiesBean11 {

    @RefreshField(false)
    private int timeout;

    private int batch;

    @RefreshField(false)
    public void setBatch(int batch) {
        this.batch = batch;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getBatch() {
        return batch;
    }
}
