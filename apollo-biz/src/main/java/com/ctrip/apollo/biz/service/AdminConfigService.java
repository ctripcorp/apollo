package com.ctrip.apollo.biz.service;

import com.ctrip.apollo.core.dto.Config4PortalDTO;
import com.ctrip.apollo.core.dto.VersionDTO;

import java.util.List;

/**
 * config service for admin
 */
public interface AdminConfigService {

    /**
     * load config info by appId and versionId
     * @param appId
     * @param versionId
     * @return
     */
    Config4PortalDTO loadReleaseConfig(long appId, long versionId);

    /**
     * 获取项目的最新配置信息
     * @param appId
     * @return
     */
    Config4PortalDTO loadLatestConfig(long appId);

    /**
     * find app's all versions
     * @param appId
     * @return
     */
    List<VersionDTO> findVersionsByApp(long appId);



}
