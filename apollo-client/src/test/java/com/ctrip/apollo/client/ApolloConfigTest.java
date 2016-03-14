package com.ctrip.apollo.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class ApolloConfigTest {
    private ApolloConfig apolloConfig;
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private ConfigurableApplicationContext applicationContext;
    @Mock
    private ConfigurableEnvironment env;
    @Mock
    private MutablePropertySources mutablePropertySources;

    @Before
    public void setUp() {
        apolloConfig = new ApolloConfig();

        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getPropertySources()).thenReturn(mutablePropertySources);

        apolloConfig.setApplicationContext(applicationContext);
        ReflectionTestUtils.setField(apolloConfig, "configLoader", configLoader);
    }

    @Test
    public void testPreparePropertySourceSuccessful() {
        CompositePropertySource somePropertySource = mock(CompositePropertySource.class);
        final ArgumentCaptor<CompositePropertySource> captor = ArgumentCaptor.forClass(CompositePropertySource.class);

        when(configLoader.loadPropertySource()).thenReturn(somePropertySource);

        apolloConfig.preparePropertySource();

        verify(configLoader, times(1)).loadPropertySource();
        verify(mutablePropertySources, times(1)).addFirst(captor.capture());

        final CompositePropertySource insertedPropertySource = captor.getValue();

        assertEquals(ApolloConfig.APOLLO_PROPERTY_SOURCE_NAME, insertedPropertySource.getName());
        assertTrue(insertedPropertySource.getPropertySources().contains(somePropertySource));
    }

}
