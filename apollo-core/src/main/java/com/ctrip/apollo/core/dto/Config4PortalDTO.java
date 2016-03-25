package com.ctrip.apollo.core.dto;



import java.util.LinkedList;
import java.util.List;

public class Config4PortalDTO {

    private long appId;

    /**
     * latest version if version is zero, or is release version
     */
    private long versionId;

    /**
     * default cluster and app self’s configs
     */
    private List<ConfigItemDTO> defaultClusterConfigs;

    /**
     * default cluster and override other app configs
     */
    private List<OverrideAppConfig> overrideAppConfigs;

    /**
     * configs in different cluster maybe different.
     * overrideClusterConfigs only save diff configs from default cluster.
     * For example:
     *      default cluster has 3 configs:
     *      {a -> A, b -> B, c -> C}
     *
     *      cluster1 has 1 config
     *      {b -> D}
     *
     * if client read cluster1 configs will return {a -> A, b -> D, c -> C}
     *
     *
     */
    private List<OverrideClusterConfig> overrideClusterConfigs;

    public Config4PortalDTO(){

    }

    public static Config4PortalDTO newInstance(long appId, long versionId){
        Config4PortalDTO instance = new Config4PortalDTO();
        instance.setAppId(appId);
        instance.setVersionId(versionId);
        instance.setDefaultClusterConfigs(new LinkedList<>());
        instance.setOverrideAppConfigs(new LinkedList<>());
        instance.setOverrideClusterConfigs(new LinkedList<>());
        return instance;
    }

    public boolean isLatestVersion() {
        return versionId == 0;
    }

    public static class OverrideAppConfig {

        private long appId;
        private List<ConfigItemDTO> configs;

        public OverrideAppConfig(){

        }

        public long getAppId() {
            return appId;
        }

        public void setAppId(long appId) {
            this.appId = appId;
        }

        public List<ConfigItemDTO> getConfigs() {
            return configs;
        }

        public void setConfigs(List<ConfigItemDTO> configs) {
            this.configs = configs;
        }

        public void addConfig(ConfigItemDTO config){
            if (configs == null){
                configs = new LinkedList<>();
            }
            configs.add(config);
        }
    }


    public static class OverrideClusterConfig {

        private String clusterName;
        private List<ConfigItemDTO> configs;

        public OverrideClusterConfig(){}

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public List<ConfigItemDTO> getConfigs() {
            return configs;
        }

        public void setConfigs(List<ConfigItemDTO> configs) {
            this.configs = configs;
        }
    }




    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }

    public List<ConfigItemDTO> getDefaultClusterConfigs() {
        return defaultClusterConfigs;
    }

    public void setDefaultClusterConfigs(List<ConfigItemDTO> defaultClusterConfigs) {
        this.defaultClusterConfigs = defaultClusterConfigs;
    }

    public List<OverrideAppConfig> getOverrideAppConfigs() {
        return overrideAppConfigs;
    }

    public void setOverrideAppConfigs(List<OverrideAppConfig> overrideAppConfigs) {
        this.overrideAppConfigs = overrideAppConfigs;
    }

    public List<OverrideClusterConfig> getOverrideClusterConfigs() {
        return overrideClusterConfigs;
    }

    public void setOverrideClusterConfigs(List<OverrideClusterConfig> overrideClusterConfigs) {
        this.overrideClusterConfigs = overrideClusterConfigs;
    }

    @Override
    public String toString() {
        return "Config4PortalDTO{" +
            "appId=" + appId +
            ", versionId=" + versionId +
            ", defaultClusterConfigs=" + defaultClusterConfigs +
            ", overrideAppConfigs=" + overrideAppConfigs +
            ", overrideClusterConfigs=" + overrideClusterConfigs +
            '}';
    }
}
