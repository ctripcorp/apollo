package com.ctrip.framework.apollo.portal.entity.model;

import java.util.Set;

public class NamespaceGrayDelReleaseModel extends NamespaceReleaseModel implements Verifiable {
    private Set<String> grayDelKeys;

    public Set<String> getGrayDelKeys() {
        return grayDelKeys;
    }

    public void setGrayDelKeys(Set<String> grayDelKeys) {
        this.grayDelKeys = grayDelKeys;
    }
}
