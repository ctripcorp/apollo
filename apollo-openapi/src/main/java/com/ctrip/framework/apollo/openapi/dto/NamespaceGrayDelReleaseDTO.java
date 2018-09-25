package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;

public class NamespaceGrayDelReleaseDTO extends NamespaceReleaseDTO {
    private Set<String> grayDelKeys;

    public Set<String> getGrayDelKeys() {
        return grayDelKeys;
    }

    public void setGrayDelKeys(Set<String> grayDelKeys) {
        this.grayDelKeys = grayDelKeys;
    }

}
