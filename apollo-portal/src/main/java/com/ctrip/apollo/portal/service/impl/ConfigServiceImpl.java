package com.ctrip.apollo.portal.service.impl;

import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.*;
import com.ctrip.apollo.portal.RestUtils;
import com.ctrip.apollo.portal.entity.AppConfigVO;
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
    public AppConfigVO loadReleaseConfig(long appId, long versionId) {

        if (appId <= 0 || versionId <= 0) {
            return null;
        }

        long releaseId = getReleaseIdFromVersionId(versionId);

        ReleaseSnapshotDTO[] releaseSnapShots = RestUtils.exchangeInGET(
            ADMIN_SERVICE_HOST + "/configs/release/" + releaseId, ReleaseSnapshotDTO[].class);
        if (releaseSnapShots == null || releaseSnapShots.length == 0) {
            return null;
        }

        AppConfigVO appConfigVO = AppConfigVO.newInstance(appId, versionId);

        for (ReleaseSnapshotDTO snapShot : releaseSnapShots) {
            //default cluster
            if (Constants.DEFAULT_CLUSTER_NAME.equals(snapShot.getClusterName())) {

                collectDefaultClusterConfigs(appId, snapShot, appConfigVO);

            } else {//cluster special configs
                collectSpecialClusterConfigs(appId, snapShot, appConfigVO);
            }
        }
        return appConfigVO;
    }

    private long getReleaseIdFromVersionId(long versionId) {
        VersionDTO version =
            RestUtils.exchangeInGET(ADMIN_SERVICE_HOST + "/version/" + versionId, VersionDTO.class);
        if (version == null) {
            return -1;
        }
        return version.getReleaseId();
    }

    private void collectDefaultClusterConfigs(long appId, ReleaseSnapshotDTO snapShot, AppConfigVO appConfigVO) {

        Map<Long, List<ConfigItemDTO>> groupedConfigs =
            groupConfigsByApp(snapShot.getConfigurations());

        List<AppConfigVO.OverrideAppConfig> overrideAppConfigs =
            appConfigVO.getOverrideAppConfigs();

        for (Map.Entry<Long, List<ConfigItemDTO>> entry : groupedConfigs.entrySet()) {
            long configAppId = entry.getKey();
            List<ConfigItemDTO> kvs = entry.getValue();

            if (configAppId == appId) {
                appConfigVO.setDefaultClusterConfigs(kvs);
            } else {

                AppConfigVO.OverrideAppConfig overrideAppConfig =
                    new AppConfigVO.OverrideAppConfig();
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

    private void collectSpecialClusterConfigs(long appId, ReleaseSnapshotDTO snapShot, AppConfigVO appConfigVO) {
        List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs =
            appConfigVO.getOverrideClusterConfigs();
        AppConfigVO.OverrideClusterConfig overrideClusterConfig =
            new AppConfigVO.OverrideClusterConfig();
        overrideClusterConfig.setClusterName(snapShot.getClusterName());
        //todo step1: cluster special config can't override other app config
        overrideClusterConfig.setConfigs(groupConfigsByApp(snapShot.getConfigurations()).get(appId));
        overrideClusterConfigs.add(overrideClusterConfig);
    }

    @Override
    public AppConfigVO loadLatestConfig(long appId) {
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

        return buildAPPConfigVO(appId, Arrays.asList(configItems));
    }

    private AppConfigVO buildAPPConfigVO(long appId, List<ConfigItemDTO> configItems) {
        if (configItems == null || configItems.size() == 0) {
            return null;
        }

        Map<String, List<ConfigItemDTO>> groupedClusterConfigs = groupConfigByCluster(configItems);

        AppConfigVO appConfigVO =
            AppConfigVO.newInstance(appId, Constants.LASTEST_VERSION_ID);

        groupConfigByAppAndEnrichDTO(groupedClusterConfigs, appConfigVO);

        return appConfigVO;

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

    private void groupConfigByAppAndEnrichDTO(Map<String, List<ConfigItemDTO>> groupedClusterConfigs, AppConfigVO appConfigVO) {
        long appId = appConfigVO.getAppId();

        List<ConfigItemDTO> defaultClusterConfigs = appConfigVO.getDefaultClusterConfigs();

        List<AppConfigVO.OverrideAppConfig> overrideAppConfigs =
            appConfigVO.getOverrideAppConfigs();

        List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs =
            appConfigVO.getOverrideClusterConfigs();

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

    private void collectDefaultClusterConfigs(long appId, List<ConfigItemDTO> clusterConfigs, List<ConfigItemDTO> defaultClusterConfigs, List<AppConfigVO.OverrideAppConfig> overrideAppConfigs) {

        Map<Long, AppConfigVO.OverrideAppConfig> appIdMapOverrideAppConfig = null;

        for (ConfigItemDTO config : clusterConfigs) {
            long targetAppId = config.getAppId();
            if (appId == targetAppId) {//app self's configs
                defaultClusterConfigs.add(config);
            } else {//override other app configs
                if (appIdMapOverrideAppConfig == null) {
                    appIdMapOverrideAppConfig = new HashMap<>();
                }

                AppConfigVO.OverrideAppConfig overrideAppConfig =
                    appIdMapOverrideAppConfig.get(targetAppId);

                if (overrideAppConfig == null) {
                    overrideAppConfig = new AppConfigVO.OverrideAppConfig();
                    appIdMapOverrideAppConfig.put(targetAppId, overrideAppConfig);
                    overrideAppConfigs.add(overrideAppConfig);
                }

                overrideAppConfig.setAppId(targetAppId);
                overrideAppConfig.addConfig(config);
            }
        }
    }

    private void collectSpecialClusterConfigs(String clusterName, List<ConfigItemDTO> clusterConfigs, List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs) {
        AppConfigVO.OverrideClusterConfig overrideClusterConfig =
            new AppConfigVO.OverrideClusterConfig();
        overrideClusterConfig.setClusterName(clusterName);
        overrideClusterConfig.setConfigs(clusterConfigs);
        overrideClusterConfigs.add(overrideClusterConfig);
    }
}
