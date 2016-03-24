package com.ctrip.apollo.biz.entity;

import com.ctrip.apollo.core.dto.ConfigItemDTO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Where(clause = "isDeleted = 0")
@SQLDelete(sql = "Update ConfigItem set isDeleted = 1 where id = ?")
public class ConfigItem {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private long clusterId;

    @Column(nullable = false)
    private String clusterName;

    @Column(nullable = false)
    private long appId;

    @Column(nullable = false)
    private String key;

    @Column
    private String value;

    @Column
    private String comment;

    @Column
    private String dataChangeCreatedBy;

    @Column
    private Date dataChangeCreatedTime;

    @Column
    private String dataChangeLastModifiedBy;

    @Column
    private Date dataChangeLastModifiedTime;

    @Column
    private boolean IsDeleted;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        return dataChangeLastModifiedBy;
    }

    public void setDataChangeLastModifiedBy(String dataChangeLastModifiedBy) {
        this.dataChangeLastModifiedBy = dataChangeLastModifiedBy;
    }

    public boolean isDeleted() {
        return IsDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        IsDeleted = isDeleted;
    }

    public Date getDataChangeLastModifiedTime() {
        return dataChangeLastModifiedTime;
    }

    public void setDataChangeLastModifiedTime(Date dataChangeLastModifiedTime) {
        this.dataChangeLastModifiedTime = dataChangeLastModifiedTime;
    }

    public ConfigItemDTO toDTO(){
        ConfigItemDTO dto = new ConfigItemDTO();
        dto.setAppId(appId);
        dto.setId(id);
        dto.setClusterId(clusterId);
        dto.setClusterName(clusterName);
        dto.setDataChangeCreatedBy(dataChangeCreatedBy);
        dto.setDataChangeLastModifiedBy(dataChangeLastModifiedBy);
        dto.setDataChangeCreatedTime(dataChangeCreatedTime);
        dto.setDataChangeLastModifiedTime(dataChangeLastModifiedTime);
        dto.setKey(key);
        dto.setValue(value);
        dto.setComment(comment);
        return dto;
    }
}
