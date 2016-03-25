package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.*;
import com.ctrip.apollo.portal.RestUtils;
import com.ctrip.apollo.portal.service.impl.ConfigServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity releaseSnapShotResponse;
    @Mock
    private ResponseEntity versionResponse;
    @Mock
    private ResponseEntity clusterResponse;
    @Mock
    private ResponseEntity configItemResponse;

    private ConfigServiceImpl configService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(RestUtils.class, "restTemplate", restTemplate);
        configService = new ConfigServiceImpl();
    }

    @Test
    public void testLoadReleaseConfig() {
        long appId = 6666;
        long versionId = 100;
        long releaseId = 11111;

        VersionDTO someVersion = assembleVersion(appId, "1.0", releaseId);
        ReleaseSnapshotDTO[] someReleaseSnapShots = assembleReleaseSnapShots();

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/configs/release/"
            + releaseId, ReleaseSnapshotDTO[].class, someReleaseSnapShots, releaseSnapShotResponse);

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/version/"
            + versionId, VersionDTO.class, someVersion, versionResponse);

        Config4PortalDTO config4PortalDTO = configService.loadReleaseConfig(appId, versionId);

        assertEquals(config4PortalDTO.getAppId(), appId);
        assertEquals(config4PortalDTO.getVersionId(), versionId);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 2);
        assertEquals(config4PortalDTO.getOverrideAppConfigs().size(), 2);
        assertEquals(config4PortalDTO.getOverrideClusterConfigs().size(), 2);
    }

    @Test
    public void testLoadReleaseConfigOnlyDefaultConfigs() {
        long appId = 6666;
        long versionId = 100;
        long releaseId = 11111;

        VersionDTO someVersion = assembleVersion(appId, "1.0", releaseId);
        ReleaseSnapshotDTO[] releaseSnapShots = new ReleaseSnapshotDTO[1];
        releaseSnapShots[0] =
            assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}");

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/configs/release/"
            + releaseId, ReleaseSnapshotDTO[].class, releaseSnapShots, releaseSnapShotResponse);

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/version/"
            + versionId, VersionDTO.class, someVersion, versionResponse);

        Config4PortalDTO config4PortalDTO = configService.loadReleaseConfig(appId, versionId);

        assertEquals(config4PortalDTO.getAppId(), appId);
        assertEquals(config4PortalDTO.getVersionId(), versionId);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 2);
        assertEquals(config4PortalDTO.getOverrideAppConfigs().size(), 0);
        assertEquals(config4PortalDTO.getOverrideClusterConfigs().size(), 0);
    }

    @Test
    public void testLoadReleaseConfigDefaultConfigsAndOverrideApp() {
        long appId = 6666;
        long versionId = 100;
        long releaseId = 11111;
        VersionDTO someVersion = assembleVersion(appId, "1.0", releaseId);
        ReleaseSnapshotDTO[] releaseSnapShots = new ReleaseSnapshotDTO[1];
        releaseSnapShots[0] =
            assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\", \"5555.bar\":\"demo2\", \"22.bar\":\"demo2\"}");

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/configs/release/"
            + releaseId, ReleaseSnapshotDTO[].class, releaseSnapShots, releaseSnapShotResponse);

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/version/"
            + versionId, VersionDTO.class, someVersion, versionResponse);


        Config4PortalDTO config4PortalDTO = configService.loadReleaseConfig(appId, versionId);


        assertEquals(config4PortalDTO.getAppId(), appId);
        assertEquals(config4PortalDTO.getVersionId(), versionId);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 2);
        assertEquals(2, config4PortalDTO.getOverrideAppConfigs().size());
        assertEquals(config4PortalDTO.getOverrideClusterConfigs().size(), 0);
    }

    @Test
    public void testLoadReleaseConfigDefaultConfigsAndOverrideCluster() {
        long appId = 6666;
        long versionId = 100;
        long releaseId = 11111;
        VersionDTO someVersion = assembleVersion(appId, "1.0", releaseId);
        ReleaseSnapshotDTO[] releaseSnapShots = new ReleaseSnapshotDTO[2];
        releaseSnapShots[0] =
            assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}");
        releaseSnapShots[1] =
            assembleReleaseSnapShot(11112, "cluster1", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}");

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/configs/release/"
            + releaseId, ReleaseSnapshotDTO[].class, releaseSnapShots, releaseSnapShotResponse);

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/version/"
            + versionId, VersionDTO.class, someVersion, versionResponse);

        Config4PortalDTO config4PortalDTO = configService.loadReleaseConfig(appId, versionId);

        assertEquals(config4PortalDTO.getAppId(), appId);
        assertEquals(config4PortalDTO.getVersionId(), versionId);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 2);
        assertEquals(0, config4PortalDTO.getOverrideAppConfigs().size());
        assertEquals(1, config4PortalDTO.getOverrideClusterConfigs().size());
    }

    @Test
    public void testLoadLastestConfig() {
        long appId = 6666;
        ClusterDTO[] someClusters = assembleClusters();
        ConfigItemDTO[] someConfigItem = assembleConfigItems();

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST + "/cluster/app/"
            + appId, ClusterDTO[].class, someClusters, clusterResponse);

        restInvoke(ConfigServiceImpl.ADMIN_SERVICE_HOST
            + "/configs/latest?clusterIds=100,101", ConfigItemDTO[].class, someConfigItem, configItemResponse);

        Config4PortalDTO config4PortalDTO = configService.loadLatestConfig(appId);

        assertEquals(config4PortalDTO.getAppId(), 6666);
        assertEquals(config4PortalDTO.getVersionId(), Constants.LASTEST_VERSION_ID);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 3);
        assertEquals(config4PortalDTO.getOverrideAppConfigs().size(), 1);
        assertEquals(config4PortalDTO.getOverrideClusterConfigs().size(), 1);
    }

    private <T> void restInvoke(String url, Class<T> responseType, T result, ResponseEntity someResponse) {
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(responseType))).thenReturn(someResponse);
        when(someResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(someResponse.getBody()).thenReturn(result);
    }

    private VersionDTO assembleVersion(long appId, String versionName, long releaseId) {
        VersionDTO version = new VersionDTO();
        version.setAppId(appId);
        version.setName(versionName);
        version.setReleaseId(releaseId);
        return version;
    }

    private ReleaseSnapshotDTO[] assembleReleaseSnapShots() {
        ReleaseSnapshotDTO[] releaseSnapShots = new ReleaseSnapshotDTO[3];
        releaseSnapShots[0] =
            assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\",\"3333.foo\":\"1008\",\"4444.bar\":\"99901\"}");
        releaseSnapShots[1] =
            assembleReleaseSnapShot(11111, "cluster1", "{\"6666.foo\":\"demo1\"}");
        releaseSnapShots[2] =
            assembleReleaseSnapShot(11111, "cluster2", "{\"6666.bar\":\"bar2222\"}");
        return releaseSnapShots;
    }

    private ReleaseSnapshotDTO assembleReleaseSnapShot(long releaseId, String clusterName, String configurations) {
        ReleaseSnapshotDTO releaseSnapShot = new ReleaseSnapshotDTO();
        releaseSnapShot.setReleaseId(releaseId);
        releaseSnapShot.setClusterName(clusterName);
        releaseSnapShot.setConfigurations(configurations);
        return releaseSnapShot;
    }

    private ClusterDTO[] assembleClusters() {
        ClusterDTO[] clusters = new ClusterDTO[2];
        clusters[0] = assembleCluster(100, 6666, "default-cluster-name");
        clusters[1] = assembleCluster(101, 6666, "cluster1");
        return clusters;
    }

    private ClusterDTO assembleCluster(long id, long appId, String name) {
        ClusterDTO cluster = new ClusterDTO();
        cluster.setAppId(appId);
        cluster.setId(id);
        cluster.setName(name);
        return cluster;
    }

    private ConfigItemDTO[] assembleConfigItems() {
        ConfigItemDTO[] configItems = new ConfigItemDTO[5];
        configItems[0] =
            assembleConfigItem(100, "default-cluster-name", 6666, "6666.k1", "6666.v1");
        configItems[1] =
            assembleConfigItem(100, "default-cluster-name", 6666, "6666.k2", "6666.v2");
        configItems[2] =
            assembleConfigItem(100, "default-cluster-name", 6666, "6666.k3", "6666.v3");
        configItems[3] =
            assembleConfigItem(100, "default-cluster-name", 5555, "5555.k1", "5555.v1");
        configItems[4] = assembleConfigItem(101, "cluster1", 6666, "6666.k1", "6666.v1");
        return configItems;
    }

    private ConfigItemDTO assembleConfigItem(long clusterId, String clusterName, int appId, String key, String value) {
        ConfigItemDTO configItem = new ConfigItemDTO();
        configItem.setClusterName(clusterName);
        configItem.setClusterId(clusterId);
        configItem.setAppId(appId);
        configItem.setKey(key);
        configItem.setValue(value);
        return configItem;
    }

}
