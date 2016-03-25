package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.portal.entity.AppConfigVO;

public interface ConfigService {

    /**
     * load config info by appId and versionId
     * @param appId
     * @param versionId
     * @return
     */
    AppConfigVO loadReleaseConfig(long appId, long versionId);

    /**
     *
     * @param appId
     * @return
     */
    AppConfigVO loadLatestConfig(long appId);


}
