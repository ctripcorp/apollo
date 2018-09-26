package com.ctrip.framework.apollo.openapi.dto;

import java.util.Set;

public class OpenGrayReleaseRuleDTO extends BaseDTO{
    private String appId;

    private String clusterName;

    private String namespaceName;

    private String branchName;

    private Set<OpenGrayReleaseRuleItemDTO> ruleItems;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Set<OpenGrayReleaseRuleItemDTO> getRuleItems() {
        return ruleItems;
    }

    public void setRuleItems(Set<OpenGrayReleaseRuleItemDTO> ruleItems) {
        this.ruleItems = ruleItems;
    }
}
