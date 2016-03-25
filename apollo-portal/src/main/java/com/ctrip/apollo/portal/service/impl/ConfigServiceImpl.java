package com.ctrip.apollo.portal.service.impl;

import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.*;
import com.ctrip.apollo.portal.RestUtils;
import com.ctrip.apollo.portal.service.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ConfigServiceImpl implements ConfigService {
    public static final String ADMIN_SERVICE_HOST = "http://localhost:8090";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Config4PortalDTO loadReleaseConfig(long appId, long versionId) {

        if (appId <= 0 || versionId <= 0) {
            return null;
        }

        long releaseId = getReleaseIdFromVersionId(versionId);

        ReleaseSnapshotDTO[] releaseSnapShots = RestUtils.exchangeInGET(
            ADMIN_SERVICE_HOST + "/configs/release/" + releaseId, ReleaseSnapshotDTO[].class);
        if (releaseSnapShots == null || releaseSnapShots.length == 0) {
            return null;
        }

        Config4PortalDTO config4PortalDTO = Config4PortalDTO.newInstance(appId, versionId);

        for (ReleaseSnapshotDTO snapShot : releaseSnapShots) {
            //default cluster
            if (Constants.DEFAULT_CLUSTER_NAME.equals(snapShot.getClusterName())) {

                collectDefaultClusterConfigs(appId, snapShot, config4PortalDTO);

            } else {//cluster special configs
                collectSpecialClusterConfigs(appId, snapShot, config4PortalDTO);
            }
        }
        return config4PortalDTO;
    }

    private long getReleaseIdFromVersionId(long versionId) {
        VersionDTO version =
            RestUtils.exchangeInGET(ADMIN_SERVICE_HOST + "/version/" + versionId, VersionDTO.class);
        if (version == null) {
            return -1;
        }
        return version.getReleaseId();
    }

    private void collectDefaultClusterConfigs(long appId, ReleaseSnapshotDTO snapShot, Config4PortalDTO config4PortalDTO) {

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
        Map<String, String> kvMaps = null;
        try {
            kvMaps = objectMapper.readValue(configJson, Map.class);
        } catch (IOException e) {
            //todo log
        }
        for (Map.Entry<String, String> entry : kvMaps.entrySet()) {
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

    private void collectSpecialClusterConfigs(long appId, ReleaseSnapshotDTO snapShot, Config4PortalDTO config4PortalDTO) {
        List<Config4PortalDTO.OverrideClusterConfig> overrideClusterConfigs =
            config4PortalDTO.getOverrideClusterConfigs();
        Config4PortalDTO.OverrideClusterConfig overrideClusterConfig =
            new Config4PortalDTO.OverrideClusterConfig();
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

        ClusterDTO[] clusters = RestUtils.exchangeInGET(
            ADMIN_SERVICE_HOST + "/cluster/app/" + appId, ClusterDTO[].class);
        if (clusters == null || clusters.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (ClusterDTO cluster : clusters) {
            sb.append(cluster.getId()).append(",");
        }

        ConfigItemDTO[] configItems = RestUtils.exchangeInGET(
            ADMIN_SERVICE_HOST + "/configs/latest?clusterIds=" + sb.substring(0,
                sb.length() - 1), ConfigItemDTO[].class);

        return buildConfig4PortalDTOFromConfigs(appId, Arrays.asList(configItems));
    }

    private Config4PortalDTO buildConfig4PortalDTOFromConfigs(long appId, List<ConfigItemDTO> configItems) {
        if (configItems == null || configItems.size() == 0) {
            return null;
        }

        Map<String, List<ConfigItemDTO>> groupedClusterConfigs = groupConfigByCluster(configItems);

        Config4PortalDTO config4PortalDTO =
            Config4PortalDTO.newInstance(appId, Constants.LASTEST_VERSION_ID);

        groupConfigByAppAndEnrichDTO(groupedClusterConfigs, config4PortalDTO);

        return config4PortalDTO;

    }

    private Map<String, List<ConfigItemDTO>> groupConfigByCluster(List<ConfigItemDTO> configItems) {
        Map<String, List<ConfigItemDTO>> groupedClusterConfigs = new HashMap<>();

        String clusterName;
        for (ConfigItemDTO configItem : configItems) {
            clusterName = configItem.getClusterName();
            List<ConfigItemDTO> clusterConfigs = groupedClusterConfigs.get(clusterName);
            if (clusterConfigs == null) {
                clusterConfigs = new LinkedList<>();
                groupedClusterConfigs.put(clusterName, clusterConfigs);
            }
            clusterConfigs.add(configItem);
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
