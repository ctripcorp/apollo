package com.ctrip.apollo.biz.entity;

import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
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
@SQLDelete(sql = "Update ReleaseSnapShot set isDeleted = 1 where id = ?")
public class ReleaseSnapShot {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private long releaseId;

    @Column(nullable = false)
    private String clusterName;

    @Column(nullable = false)
    private String configurations;

    private boolean isDeleted;

    public ReleaseSnapShot() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getConfigurations() {
        return configurations;
    }

    public void setConfigurations(String configurations) {
        this.configurations = configurations;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public ReleaseSnapshotDTO toDTO(){
        ReleaseSnapshotDTO dto = new ReleaseSnapshotDTO();
        dto.setId(id);
        dto.setClusterName(clusterName);
        dto.setConfigurations(configurations);
        dto.setReleaseId(releaseId);
        return dto;
    }
}
