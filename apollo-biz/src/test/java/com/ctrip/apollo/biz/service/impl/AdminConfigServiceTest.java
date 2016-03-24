package com.ctrip.apollo.biz.service.impl;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.entity.ReleaseSnapShot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.ConfigItemRepository;
import com.ctrip.apollo.biz.repository.ReleaseSnapShotRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.ctrip.apollo.core.Constants;
import com.ctrip.apollo.core.dto.Config4PortalDTO;
import com.ctrip.apollo.core.dto.VersionDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminConfigServiceTest {

    @Mock
    private VersionRepository versionRepository;
    @Mock
    private ReleaseSnapShotRepository releaseSnapShotRepository;
    @Mock
    private ConfigItemRepository configItemRepository;
    @Mock
    private ClusterRepository clusterRepository;

    private AdminConfigServiceImpl adminConfigService;

    @Before
    public void setUp() {
        adminConfigService = new AdminConfigServiceImpl();
        ReflectionTestUtils.setField(adminConfigService, "versionRepository", versionRepository);
        ReflectionTestUtils.setField(adminConfigService, "releaseSnapShotRepository", releaseSnapShotRepository);
        ReflectionTestUtils.setField(adminConfigService, "configItemRepository", configItemRepository);
        ReflectionTestUtils.setField(adminConfigService, "clusterRepository", clusterRepository);
    }

    @Test
    public void testLoadReleaseConfig() {
        long appId = 6666;
        long versionId = 100;
        long releaseId = 11111;
        Version someVersion = assembleVersion(appId, "1.0", releaseId);
        List<ReleaseSnapShot> someReleaseSnapShots = assembleReleaseSnapShots();

        when(versionRepository.findById(versionId)).thenReturn(someVersion);
        when(releaseSnapShotRepository.findByReleaseId(releaseId)).thenReturn(someReleaseSnapShots);

        Config4PortalDTO config4PortalDTO = adminConfigService.loadReleaseConfig(appId, versionId);

        verify(versionRepository, times(1)).findById(versionId);
        verify(releaseSnapShotRepository, times(1)).findByReleaseId(releaseId);

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
        Version someVersion = assembleVersion(appId, "1.0", releaseId);
        List<ReleaseSnapShot> releaseSnapShots = new ArrayList<>();
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}"));

        when(versionRepository.findById(versionId)).thenReturn(someVersion);
        when(releaseSnapShotRepository.findByReleaseId(releaseId)).thenReturn(releaseSnapShots);

        Config4PortalDTO config4PortalDTO = adminConfigService.loadReleaseConfig(appId, versionId);

        verify(versionRepository, times(1)).findById(versionId);
        verify(releaseSnapShotRepository, times(1)).findByReleaseId(releaseId);

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
        Version someVersion = assembleVersion(appId, "1.0", releaseId);
        List<ReleaseSnapShot> releaseSnapShots = new ArrayList<>();
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\", \"5555.bar\":\"demo2\", \"22.bar\":\"demo2\"}"));

        when(versionRepository.findById(versionId)).thenReturn(someVersion);
        when(releaseSnapShotRepository.findByReleaseId(releaseId)).thenReturn(releaseSnapShots);

        Config4PortalDTO config4PortalDTO = adminConfigService.loadReleaseConfig(appId, versionId);

        verify(versionRepository, times(1)).findById(versionId);
        verify(releaseSnapShotRepository, times(1)).findByReleaseId(releaseId);

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
        Version someVersion = assembleVersion(appId, "1.0", releaseId);
        List<ReleaseSnapShot> releaseSnapShots = new ArrayList<>();
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}"));
        releaseSnapShots.add(assembleReleaseSnapShot(11112, "cluster1", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\"}"));

        when(versionRepository.findById(versionId)).thenReturn(someVersion);
        when(releaseSnapShotRepository.findByReleaseId(releaseId)).thenReturn(releaseSnapShots);

        Config4PortalDTO config4PortalDTO = adminConfigService.loadReleaseConfig(appId, versionId);

        verify(versionRepository, times(1)).findById(versionId);
        verify(releaseSnapShotRepository, times(1)).findByReleaseId(releaseId);

        assertEquals(config4PortalDTO.getAppId(), appId);
        assertEquals(config4PortalDTO.getVersionId(), versionId);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 2);
        assertEquals(0, config4PortalDTO.getOverrideAppConfigs().size());
        assertEquals(1, config4PortalDTO.getOverrideClusterConfigs().size());
    }

    @Test
    public void testLoadLastestConfig() {
        long appId = 6666;
        List<Long> clusterIds = Arrays.asList(Long.valueOf(100), Long.valueOf(101));
        List<Cluster> someClusters = assembleClusters();
        List<ConfigItem> someConfigItem = assembleConfigItems();

        when(clusterRepository.findByAppId(appId)).thenReturn(someClusters);
        when(configItemRepository.findByClusterIdIsIn(clusterIds)).thenReturn(someConfigItem);

        Config4PortalDTO config4PortalDTO = adminConfigService.loadLatestConfig(appId);

        verify(clusterRepository, times(1)).findByAppId(appId);
        verify(configItemRepository, times(1)).findByClusterIdIsIn(clusterIds);

        assertEquals(config4PortalDTO.getAppId(), 6666);
        assertEquals(config4PortalDTO.getVersionId(), Constants.LASTEST_VERSION_ID);
        assertEquals(config4PortalDTO.getDefaultClusterConfigs().size(), 3);
        assertEquals(config4PortalDTO.getOverrideAppConfigs().size(), 1);
        assertEquals(config4PortalDTO.getOverrideClusterConfigs().size(), 1);
    }

    @Test
    public void testFindVersionsByApp() {
        long appId = 6666;

        List<Version> someVersions = assembleVersions();

        when(versionRepository.findByAppId(appId)).thenReturn(someVersions);

        List<VersionDTO> versionDTOs = adminConfigService.findVersionsByApp(appId);

        verify(versionRepository, times(1)).findByAppId(appId);

        assertEquals(versionDTOs.size(), 5);

    }

    private List<Version> assembleVersions() {
        List<Version> versions = new ArrayList<>();
        versions.add(assembleVersion(6666, "1.0", 11111));
        versions.add(assembleVersion(6666, "2.0", 11112));
        versions.add(assembleVersion(6666, "3.0", 11113));
        versions.add(assembleVersion(6666, "4.0", 11114));
        versions.add(assembleVersion(6666, "5.0", 11115));
        return versions;
    }

    private Version assembleVersion(long appId, String versionName, long releaseId) {
        Version version = new Version();
        version.setAppId(appId);
        version.setName(versionName);
        version.setReleaseId(releaseId);
        return version;
    }


    private List<ReleaseSnapShot> assembleReleaseSnapShots() {
        List<ReleaseSnapShot> releaseSnapShots = new ArrayList<>();
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "default-cluster-name", "{\"6666.foo\":\"demo1\", \"6666.bar\":\"demo2\",\"3333.foo\":\"1008\",\"4444.bar\":\"99901\"}"));
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "cluster1", "{\"6666.foo\":\"demo1\"}"));
        releaseSnapShots.add(assembleReleaseSnapShot(11111, "cluster2", "{\"6666.bar\":\"bar2222\"}"));
        //        releaseSnapShots.add(assembleReleaseSnapShot(11112, "default-cluster-name", "{\"6666.foo\":\"verson2.0\", \"6666.bar\":\"verson2.0\",\"3333.foo\":\"1008\",\"4444.bar\":\"99901\"}"));
        return releaseSnapShots;
    }

    private ReleaseSnapShot assembleReleaseSnapShot(long releaseId, String clusterName, String configurations) {
        ReleaseSnapShot releaseSnapShot = new ReleaseSnapShot();
        releaseSnapShot.setReleaseId(releaseId);
        releaseSnapShot.setClusterName(clusterName);
        releaseSnapShot.setConfigurations(configurations);
        return releaseSnapShot;
    }

    private List<Cluster> assembleClusters() {
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(assembleCluster(100, 6666, "default-cluster-name"));
        clusters.add(assembleCluster(101, 6666, "cluster1"));
        return clusters;
    }

    private Cluster assembleCluster(long id, long appId, String name) {
        Cluster cluster = new Cluster();
        cluster.setAppId(appId);
        cluster.setId(id);
        cluster.setName(name);
        return cluster;
    }

    private List<ConfigItem> assembleConfigItems() {
        List<ConfigItem> configItems = new ArrayList<>();
        configItems.add(assembleConfigItem(100, "default-cluster-name", 6666, "6666.k1", "6666.v1"));
        configItems.add(assembleConfigItem(100, "default-cluster-name", 6666, "6666.k2", "6666.v2"));
        configItems.add(assembleConfigItem(100, "default-cluster-name", 6666, "6666.k3", "6666.v3"));
        configItems.add(assembleConfigItem(100, "default-cluster-name", 5555, "5555.k1", "5555.v1"));
        configItems.add(assembleConfigItem(101, "cluster1", 6666, "6666.k1", "6666.v1"));
        return configItems;
    }

    private ConfigItem assembleConfigItem(long clusterId, String clusterName, int appId, String key, String value) {
        ConfigItem configItem = new ConfigItem();
        configItem.setClusterName(clusterName);
        configItem.setClusterId(clusterId);
        configItem.setAppId(appId);
        configItem.setKey(key);
        configItem.setValue(value);
        return configItem;
    }
}
