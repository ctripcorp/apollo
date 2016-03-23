package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigServiceLocater;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.serivce.ApolloService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteConfigLoaderTest {
    private RemoteConfigLoader remoteConfigLoader;
    @Mock
    private RestTemplate restTemplate;
    private ConfigUtil configUtil;
    @Mock
    private ConfigServiceLocater serviceLocater;

    @Before
    public void setUp() {
        configUtil = spy(ConfigUtil.getInstance());
        remoteConfigLoader = spy(new RemoteConfigLoader(restTemplate, configUtil,serviceLocater));
    }

    @Test
    public void testLoadPropertySource() throws Exception {
        long someAppId = 100;
        long anotherAppId = 101;
        ApolloRegistry someApolloRegistry = assembleSomeApolloRegistry(someAppId, "someVersion");
        ApolloRegistry anotherApolloRegistry = assembleSomeApolloRegistry(anotherAppId, "anotherVersion");
        MapPropertySource somePropertySource = mock(MapPropertySource.class);
        MapPropertySource anotherPropertySource = mock(MapPropertySource.class);

        doReturn(Lists.newArrayList(someApolloRegistry, anotherApolloRegistry)).when(configUtil).loadApolloRegistries();
        doReturn(somePropertySource).when(remoteConfigLoader).loadSingleApolloConfig(someApolloRegistry.getAppId(), someApolloRegistry.getVersion());
        doReturn(anotherPropertySource).when(remoteConfigLoader).loadSingleApolloConfig(anotherApolloRegistry.getAppId(), anotherApolloRegistry.getVersion());

        CompositePropertySource result = remoteConfigLoader.loadPropertySource();

        assertEquals(2, result.getPropertySources().size());
        assertTrue(result.getPropertySources().containsAll(Lists.newArrayList(somePropertySource, anotherPropertySource)));
    }

    @Test
    public void testLoadPropertySourceWithNoApolloRegistry() throws Exception {
        doReturn(null).when(configUtil).loadApolloRegistries();

        CompositePropertySource result = remoteConfigLoader.loadPropertySource();

        assertTrue(result.getPropertySources().isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadPropertySourceWithError() throws Exception {
        Exception someException = mock(Exception.class);
        long someAppId = 100;
        ApolloRegistry someApolloRegistry = assembleSomeApolloRegistry(someAppId, "someVersion");
        doReturn(Lists.newArrayList(someApolloRegistry)).when(configUtil).loadApolloRegistries();

        doThrow(someException).when(remoteConfigLoader).loadSingleApolloConfig(someApolloRegistry.getAppId(), someApolloRegistry.getVersion());

        remoteConfigLoader.loadPropertySource();
    }

    @Test
    public void testLoadSingleApolloConfig() throws Exception {
        ApolloConfig someApolloConfig = mock(ApolloConfig.class);
        Map<String, Object> someMap = Maps.newHashMap();
        List<ApolloService> someService = Lists.newArrayList();
        someService.add(new ApolloService());
        
        when(someApolloConfig.getConfigurations()).thenReturn(someMap);
        doReturn(someApolloConfig).when(remoteConfigLoader).getRemoteConfig(any(RestTemplate.class), anyString(), anyLong(), anyString(), anyString());

        long someAppId = 100;
        when(serviceLocater.getConfigServices()).thenReturn(someService);
        MapPropertySource result = remoteConfigLoader.loadSingleApolloConfig(someAppId, "someVersion");

        assertEquals(someMap, result.getSource());
    }

    private ApolloRegistry assembleSomeApolloRegistry(long someAppId, String someVersion) {
        ApolloRegistry someApolloRegistry = new ApolloRegistry();
        someApolloRegistry.setAppId(someAppId);
        someApolloRegistry.setVersion(someVersion);

        return someApolloRegistry;
    }
}
