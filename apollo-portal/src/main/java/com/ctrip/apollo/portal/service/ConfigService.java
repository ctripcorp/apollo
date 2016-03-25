package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.core.dto.Config4PortalDTO;

public interface ConfigService {

    /**
     * load config info by appId and versionId
     * @param appId
     * @param versionId
     * @return
     */
    Config4PortalDTO loadReleaseConfig(long appId, long versionId);

    /**
     *
     * @param appId
     * @return
     */
    Config4PortalDTO loadLatestConfig(long appId);


}
