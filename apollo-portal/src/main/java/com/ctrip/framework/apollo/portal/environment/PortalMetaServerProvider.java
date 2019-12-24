package com.ctrip.framework.apollo.portal.environment;

import com.ctrip.framework.apollo.core.spi.Ordered;

/**
 * a fake class to replace com.ctrip.framework.apollo.core.spi.MetaServerProvider in apollo-portal
 * @see com.ctrip.framework.apollo.core.spi.MetaServerProvider
 * @author wxq
 */
public interface PortalMetaServerProvider extends Ordered {

    String getMetaServerAddress(Env targetEnv);

}
