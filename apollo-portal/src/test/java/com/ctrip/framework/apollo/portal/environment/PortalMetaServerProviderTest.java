package com.ctrip.framework.apollo.portal.environment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PortalMetaServerProviderTest {

    /**
     * may be the environments and meta server addresses
     * have been clear, so we need to reload when start the every unit test
     */
    @Before
    public void reload() {
        PortalMetaServerProvider.reloadAll();
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("dev_meta");
    }

    @Test
    public void testFromPropertyFile() {
        assertEquals("http://localhost:8080", PortalMetaServerProvider.getMetaServerAddress(Env.LOCAL));
        assertEquals("${dev_meta}", PortalMetaServerProvider.getMetaServerAddress(Env.DEV));
        assertEquals("${pro_meta}", PortalMetaServerProvider.getMetaServerAddress(Env.PRO));
    }

    /**
     * testing the environment dynamic added from system property
     */
    @Test
    public void testDynamicEnvironmentFromSystemProperty() {
        String someDevMetaAddress = "someMetaAddress";
        String someFatMetaAddress = "someFatMetaAddress";
        System.setProperty("dev_meta", someDevMetaAddress);
        System.setProperty("fat_meta", someFatMetaAddress);
        // reload above added
        PortalMetaServerProvider.reloadAll();
        assertEquals(someDevMetaAddress, PortalMetaServerProvider.getMetaServerAddress(Env.DEV));
        assertEquals(someFatMetaAddress, PortalMetaServerProvider.getMetaServerAddress(Env.FAT));

        String randomAddress = "randomAddress";
        String randomEnvironment = "randomEnvironment";
        System.setProperty(randomEnvironment + "_meta", randomAddress);
        // reload above added
        PortalMetaServerProvider.reloadAll();
        assertEquals(
                randomAddress,
                PortalMetaServerProvider.getMetaServerAddress(
                        Env.valueOf(randomEnvironment)
                )
        );

        // clear the property
        System.clearProperty(randomEnvironment + "_meta");
    }

}