package com.ctrip.apollo.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.entity.ReleaseSnapShot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.ConfigItemRepository;
import com.ctrip.apollo.biz.repository.ReleaseSnapShotRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.ctrip.apollo.biz.service.AdminConfigService;
import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.Config4PortalDTO;
import com.ctrip.apollo.core.dto.ConfigItemDTO;
import com.ctrip.apollo.core.dto.VersionDTO;
import com.google.common.collect.Maps;
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
    public Config4PortalDTO loadReleaseConfig(long appId, long versionId) {

        if (appId <= 0 || versionId <= 0) {
            return null;
        }

        List<ReleaseSnapShot> releaseSnapShots =
            releaseSnapShotRepository.findByReleaseId(getReleaseIdFromVersionId(versionId));

        if (releaseSnapShots == null || releaseSnapShots.size() == 0) {
            return null;
        }

        Config4PortalDTO config4PortalDTO = Config4PortalDTO.newInstance(appId, versionId);

        for (ReleaseSnapShot snapShot : releaseSnapShots) {
            //default cluster
            if (Constants.DEFAULT_CLUSTER_NAME.equals(snapShot.getClusterName())) {

                collectDefaultClusterConfigs(appId, snapShot, config4PortalDTO);

            } else {//cluster special configs
                collectSpecialClusterConfigs(appId, snapShot, config4PortalDTO);
            }
        }
        return config4PortalDTO;
    }


    private void collectDefaultClusterConfigs(long appId, ReleaseSnapShot snapShot, Config4PortalDTO config4PortalDTO) {

        Map<Long, List<ConfigItemDTO>> groupedConfigs =
            groupConfigsByApp(snapShot.getConfigurations());

        List<Config4PortalDTO.OverrideAppConfig> overrideAppConfigs =
            config4PortalDTO.getOverrideAppConfigs();

        for (Map.Entry<Long, List<ConfigItemDTO>> entry : groupedConfigs.entrySet()) {
            long configAppId = entry.getKey();
            List<ConfigItemDTO> kvs = entry.getValue();

            if (configAppId == appId) {
                config4PortalDTO.setDefaultClusterConfigs(kvs);
            } else {

                Config4PortalDTO.OverrideAppConfig overrideAppConfig =
                    new Config4PortalDTO.OverrideAppConfig();
                overrideAppConfig.setAppId(configAppId);
                overrideAppConfig.setConfigs(kvs);
                overrideAppConfigs.add(overrideAppConfig);
            }
        }

    }

    private void collectSpecialClusterConfigs(long appId, ReleaseSnapShot snapShot, Config4PortalDTO config4PortalDTO) {
        List<Config4PortalDTO.OverrideClusterConfig> overrideClusterConfigs =
            config4PortalDTO.getOverrideClusterConfigs();
        Config4PortalDTO.OverrideClusterConfig overrideClusterConfig = new Config4PortalDTO.OverrideClusterConfig();
        overrideClusterConfig.setClusterName(snapShot.getClusterName());
        //todo step1: cluster special config can't override other app config
        overrideClusterConfig.setConfigs(groupConfigsByApp(snapShot.getConfigurations()).get(appId));
        overrideClusterConfigs.add(overrideClusterConfig);
    }

    @Override
    public Config4PortalDTO loadLatestConfig(long appId) {
        if (appId <= 0) {
            return null;
        }

        List<Cluster> clusters = clusterRepository.findByAppId(appId);
        if (clusters == null || clusters.size() == 0) {
            return null;
        }

        List<Long> clusterIds = new ArrayList<>(clusters.size());
        for (Cluster cluster : clusters) {
            clusterIds.add(cluster.getId());
        }

        List<ConfigItem> configItems = configItemRepository.findByClusterIdIsIn(clusterIds);

        return buildConfig4PortalDTOFromConfigs(appId, configItems);
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

    private long getReleaseIdFromVersionId(long versionId) {
        Version version = versionRepository.findById(versionId);
        if (version == null) {
            return -1;
        }
        return version.getReleaseId();
    }

    /**
     * appId -> List<KV>
     */
    private Map<Long, List<ConfigItemDTO>> groupConfigsByApp(String configJson) {
        if (configJson == null || "".equals(configJson)) {
            return Maps.newHashMap();

        }

        Map<Long, List<ConfigItemDTO>> appIdMapKVs = new HashMap<>();

        String key;
        Object value;
        JSONObject kvMaps = JSON.parseObject(configJson);
        for (Map.Entry<String, Object> entry : kvMaps.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();

            Long appId = getAppIdFromKey(key);
            List<ConfigItemDTO> kvs = appIdMapKVs.get(appId);
            if (kvs == null) {
                kvs = new LinkedList<>();
                appIdMapKVs.put(appId, kvs);
            }
            kvs.add(new ConfigItemDTO(key, value.toString()));
        }

        return appIdMapKVs;

    }

    private Long getAppIdFromKey(String key) {
        return Long.valueOf(key.substring(0, key.indexOf(".")));
    }

    private Config4PortalDTO buildConfig4PortalDTOFromConfigs(long appId, List<ConfigItem> configItems) {
        if (configItems == null || configItems.size() == 0) {
            return null;
        }

        Map<String, List<ConfigItemDTO>> groupedClusterConfigs =
            groupConfigByCluster(configItems);

        Config4PortalDTO config4PortalDTO = Config4PortalDTO.newInstance(appId, Constants.LASTEST_VERSION_ID);

        groupConfigByAppAndEnrichDTO(groupedClusterConfigs, config4PortalDTO);

        return config4PortalDTO;

    }

    private Map<String, List<ConfigItemDTO>> groupConfigByCluster(List<ConfigItem> configItems) {
        Map<String, List<ConfigItemDTO>> groupedClusterConfigs = new HashMap<>();

        String clusterName;
        for (ConfigItem configItem : configItems) {
            clusterName = configItem.getClusterName();
            List<ConfigItemDTO> clusterConfigs = groupedClusterConfigs.get(clusterName);
            if (clusterConfigs == null) {
                clusterConfigs = new LinkedList<>();
                groupedClusterConfigs.put(clusterName, clusterConfigs);
            }
            clusterConfigs.add(configItem.toDTO());
        }
        return groupedClusterConfigs;
    }

    private void groupConfigByAppAndEnrichDTO(Map<String, List<ConfigItemDTO>> groupedClusterConfigs, Config4PortalDTO config4PortalDTO) {
        long appId = config4PortalDTO.getAppId();

        List<ConfigItemDTO> defaultClusterConfigs = config4PortalDTO.getDefaultClusterConfigs();

        List<Config4PortalDTO.OverrideAppConfig> overrideAppConfigs =
            config4PortalDTO.getOverrideAppConfigs();

        List<Config4PortalDTO.OverrideClusterConfig> overrideClusterConfigs =
            config4PortalDTO.getOverrideClusterConfigs();

        String clusterName;
        List<ConfigItemDTO> clusterConfigs;
        for (Map.Entry<String, List<ConfigItemDTO>> entry : groupedClusterConfigs.entrySet()) {
            clusterName = entry.getKey();
            clusterConfigs = entry.getValue();

            if (Constants.DEFAULT_CLUSTER_NAME.equals(clusterName)) {
                //default cluster configs
                collectDefaultClusterConfigs(appId, clusterConfigs, defaultClusterConfigs, overrideAppConfigs);
            } else {
                //override cluster configs
                collectSpecialClusterConfigs(clusterName, clusterConfigs, overrideClusterConfigs);
            }
        }
    }

    private void collectDefaultClusterConfigs(long appId, List<ConfigItemDTO> clusterConfigs, List<ConfigItemDTO> defaultClusterConfigs, List<Config4PortalDTO.OverrideAppConfig> overrideAppConfigs) {

        Map<Long, Config4PortalDTO.OverrideAppConfig> appIdMapOverrideAppConfig = null;

        for (ConfigItemDTO config : clusterConfigs) {
            long targetAppId = config.getAppId();
            if (appId == targetAppId) {//app self's configs
                defaultClusterConfigs.add(config);
            } else {//override other app configs
                if (appIdMapOverrideAppConfig == null) {
                    appIdMapOverrideAppConfig = new HashMap<>();
                }

                Config4PortalDTO.OverrideAppConfig overrideAppConfig =
                    appIdMapOverrideAppConfig.get(targetAppId);

                if (overrideAppConfig == null) {
                    overrideAppConfig = new Config4PortalDTO.OverrideAppConfig();
                    appIdMapOverrideAppConfig.put(targetAppId, overrideAppConfig);
                    overrideAppConfigs.add(overrideAppConfig);
                }

                overrideAppConfig.setAppId(targetAppId);
                overrideAppConfig.addConfig(config);
            }
        }
    }

    private void collectSpecialClusterConfigs(String clusterName, List<ConfigItemDTO> clusterConfigs, List<Config4PortalDTO.OverrideClusterConfig> overrideClusterConfigs) {
        Config4PortalDTO.OverrideClusterConfig overrideClusterConfig =
            new Config4PortalDTO.OverrideClusterConfig();
        overrideClusterConfig.setClusterName(clusterName);
        overrideClusterConfig.setConfigs(clusterConfigs);
        overrideClusterConfigs.add(overrideClusterConfig);
    }
}
