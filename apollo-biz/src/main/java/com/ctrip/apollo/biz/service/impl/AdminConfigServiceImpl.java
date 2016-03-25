package com.ctrip.apollo.biz.service.impl;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.entity.ReleaseSnapShot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.ConfigItemRepository;
import com.ctrip.apollo.biz.repository.ReleaseSnapShotRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.ctrip.apollo.biz.service.AdminConfigService;
import com.ctrip.apollo.core.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("adminConfigService")
public class AdminConfigServiceImpl implements AdminConfigService {

    @Autowired
    private VersionRepository versionRepository;
    @Autowired
    private ReleaseSnapShotRepository releaseSnapShotRepository;
    @Autowired
    private ClusterRepository clusterRepository;
    @Autowired
    private ConfigItemRepository configItemRepository;

    @Override
    public List<ReleaseSnapshotDTO> findReleaseSnapshotByReleaseId(long releaseId) {
        if (releaseId <= 0){
            return Collections.EMPTY_LIST;
        }

        List<ReleaseSnapShot> releaseSnapShots = releaseSnapShotRepository.findByReleaseId(releaseId);

        if (releaseSnapShots == null || releaseSnapShots.size() == 0){
            return Collections.EMPTY_LIST;
        }

        List<ReleaseSnapshotDTO> result = new ArrayList<>(releaseSnapShots.size());
        for (ReleaseSnapShot releaseSnapShot: releaseSnapShots){
            result.add(releaseSnapShot.toDTO());
        }
        return result;
    }


    @Override
    public List<VersionDTO> findVersionsByApp(long appId) {
        if (appId <= 0) {
            return Collections.EMPTY_LIST;
        }

        List<Version> versions = versionRepository.findByAppId(appId);
        if (versions == null || versions.size() == 0) {
            return Collections.EMPTY_LIST;
        }

        List<VersionDTO> result = new ArrayList<>(versions.size());
        for (Version version : versions) {
            result.add(version.toDTO());
        }
        return result;
    }

    @Override
    public VersionDTO loadVersionById(long versionId) {
        if (versionId <= 0){
            return null;
        }
        Version version = versionRepository.findById(versionId);
        return version.toDTO();
    }

    @Override
    public List<ClusterDTO> findClustersByApp(long appId) {
        if (appId <= 0){
            return Collections.EMPTY_LIST;
        }
        List<Cluster> clusters = clusterRepository.findByAppId(appId);
        if (clusters == null || clusters.size() == 0){
            return Collections.EMPTY_LIST;
        }

        List<ClusterDTO> result = new ArrayList<>(clusters.size());
        for (Cluster cluster: clusters){
            result.add(cluster.toDTO());
        }
        return result;
    }

    @Override
    public List<ConfigItemDTO> findConfigItemsByClusters(List<Long> clusterIds) {
        if (clusterIds == null || clusterIds.size() == 0){
            return Collections.EMPTY_LIST;
        }
        List<ConfigItem> configItems = configItemRepository.findByClusterIdIsIn(clusterIds);
        if (configItems == null || configItems.size() == 0){
            return Collections.EMPTY_LIST;
        }

        List<ConfigItemDTO> result = new ArrayList<>(configItems.size());
        for (ConfigItem configItem: configItems){
            result.add(configItem.toDTO());
        }
        return result;
    }

}
