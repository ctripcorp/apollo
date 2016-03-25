package com.ctrip.apollo.core.dto;

import java.util.Date;

public class ConfigItemDTO {

    private long id;

    private long clusterId;

    private String clusterName;

    private long appId;

    private String key;

    private String value;

    private String comment;

    private String dataChangeCreatedBy;

    private Date dataChangeCreatedTime;

    private String DataChangeLastModifiedBy;

    private Date dataChangeLastModifiedTime;

    public ConfigItemDTO(){

    }

    public ConfigItemDTO(String key, String value){
        this.key = key;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataChangeCreatedBy() {
        return dataChangeCreatedBy;
    }

    public void setDataChangeCreatedBy(String dataChangeCreatedBy) {
        this.dataChangeCreatedBy = dataChangeCreatedBy;
    }

    public Date getDataChangeCreatedTime() {
        return dataChangeCreatedTime;
    }

    public void setDataChangeCreatedTime(Date dataChangeCreatedTime) {
        this.dataChangeCreatedTime = dataChangeCreatedTime;
    }

    public String getDataChangeLastModifiedBy() {
        return DataChangeLastModifiedBy;
    }

    public void setDataChangeLastModifiedBy(String dataChangeLastModifiedBy) {
        DataChangeLastModifiedBy = dataChangeLastModifiedBy;
    }

    public Date getDataChangeLastModifiedTime() {
        return dataChangeLastModifiedTime;
    }

    public void setDataChangeLastModifiedTime(Date dataChangeLastModifiedTime) {
        this.dataChangeLastModifiedTime = dataChangeLastModifiedTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
