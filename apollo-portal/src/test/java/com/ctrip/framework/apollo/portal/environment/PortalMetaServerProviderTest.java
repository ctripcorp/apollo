package com.ctrip.framework.apollo.portal.environment;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PortalMetaServerProviderTest {

    @After
    public void tearDown() throws Exception {
        System.clearProperty("dev_meta");
    }

    @Test
    public void testFromPropertyFile() {
        PortalMetaServerProvider portalMetaServerProvider = new PortalMetaServerProvider();
        assertEquals("http://localhost:8080", portalMetaServerProvider.getMetaServerAddress(Env.LOCAL));
        assertEquals("${dev_meta}", portalMetaServerProvider.getMetaServerAddress(Env.DEV));
        assertEquals("${pro_meta}", portalMetaServerProvider.getMetaServerAddress(Env.PRO));
    }

    @Test
    public void testWithSystemProperty() {
        String someDevMetaAddress = "someMetaAddress";
        String someFatMetaAddress = "someFatMetaAddress";
        System.setProperty("dev_meta", someDevMetaAddress);
        System.setProperty("fat_meta", someFatMetaAddress);

        PortalMetaServerProvider portalMetaServerProvider = new PortalMetaServerProvider();

        assertEquals(someDevMetaAddress, portalMetaServerProvider.getMetaServerAddress(Env.DEV));
        assertEquals(someFatMetaAddress, portalMetaServerProvider.getMetaServerAddress(Env.FAT));
    }

    /**
     * testing the environment dynamic added from system property
     */
    @Test
    public void testDynamicEnvironmentFromSystemProperty() {
        String randomAddress = "randomAddress";
        String randomEnvironment = "randomEnvironment";

        System.setProperty(randomEnvironment + "_meta", randomAddress);

        PortalMetaServerProvider portalMetaServerProvider = new PortalMetaServerProvider();

        assertEquals(
                randomAddress,
                portalMetaServerProvider.getMetaServerAddress(
                        Env.valueOf(randomEnvironment)
                )
        );

        // clear the property
        System.clearProperty(randomEnvironment + "_meta");
    }

}