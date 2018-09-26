package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;

public class OpenGrayReleaseRuleItemDTO {
    private String clientAppId;
    private Set<String> clientIpList;

    public String getClientAppId() {
        return clientAppId;
    }

    public void setClientAppId(String clientAppId) {
        this.clientAppId = clientAppId;
    }

    public Set<String> getClientIpList() {
        return clientIpList;
    }

    public void setClientIpList(Set<String> clientIpList) {
        this.clientIpList = clientIpList;
    }
}
