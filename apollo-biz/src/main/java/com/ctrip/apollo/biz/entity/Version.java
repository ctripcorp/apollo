package com.ctrip.apollo.biz.entity;

import com.ctrip.apollo.core.dto.VersionDTO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@Where(clause = "isDeleted = 0")
@SQLDelete(sql = "Update Version set isDeleted = 1 where id = ?")
public class Version {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long appId;

    @Column(nullable = false)
    private long releaseId;
    private long parentVersion;
    private boolean isDeleted;

    public Version() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Long getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(Long parentVersion) {
        this.parentVersion = parentVersion;
    }

    public VersionDTO toDTO() {
        VersionDTO dto = new VersionDTO();
        dto.setAppId(this.appId);
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setParentVersion(this.parentVersion);
        dto.setReleaseId(this.releaseId);
        return dto;
    }
}
