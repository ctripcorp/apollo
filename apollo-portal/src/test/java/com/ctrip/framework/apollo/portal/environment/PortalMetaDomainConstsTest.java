package com.ctrip.framework.apollo.portal.environment;

import org.junit.After;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PortalMetaDomainConstsTest extends BaseIntegrationTest {

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        clear();
    }

    @Test
    public void testGetMetaDomain() {
        // local
        String localMetaServerAddress = "http://localhost:8080";
        mockMetaServerAddress(Env.LOCAL, localMetaServerAddress);
        assertEquals(localMetaServerAddress, PortalMetaDomainConsts.getDomain(Env.LOCAL));

        // add this environment without meta server address
        String randomEnvironment = "randomEnvironment";
        Env.addEnvironment(randomEnvironment);
        assertEquals(PortalMetaDomainConsts.DEFAULT_META_URL, PortalMetaDomainConsts.getDomain(Env.valueOf(randomEnvironment)));
    }

    @Test
    public void testGetValidAddress() throws Exception {
        String someResponse = "some response";
        startServerWithHandlers(mockServerHandler(HttpServletResponse.SC_OK, someResponse));

        String validServer = " http://localhost:" + PORT + " ";
        String invalidServer = "http://localhost:" + findFreePort();

        mockMetaServerAddress(Env.FAT, validServer + "," + invalidServer);
        mockMetaServerAddress(Env.UAT, invalidServer + "," + validServer);

        assertEquals(validServer.trim(), PortalMetaDomainConsts.getDomain(Env.FAT));
        assertEquals(validServer.trim(), PortalMetaDomainConsts.getDomain(Env.UAT));
    }

    @Test
    public void testInvalidAddress() {
        String invalidServer = "http://localhost:" + findFreePort() + " ";
        String anotherInvalidServer = "http://localhost:" + findFreePort() + " ";

        mockMetaServerAddress(Env.LPT, invalidServer + "," + anotherInvalidServer);

        String metaServer = PortalMetaDomainConsts.getDomain(Env.LPT);

        assertTrue(metaServer.equals(invalidServer.trim()) || metaServer.equals(anotherInvalidServer.trim()));
    }

    private void mockMetaServerAddress(Env env, String metaServerAddress) {
        PortalMetaServerProvider.getInstance().mockMetaServerAddress(env, metaServerAddress);
    }

    private void clear() {
        PortalMetaServerProvider.getInstance().reset();
    }
}