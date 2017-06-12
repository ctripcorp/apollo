package com.ctrip.framework.apollo.configservice.service;

import com.google.common.base.Strings;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.grayReleaseRule.GrayReleaseRulesHolder;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.configservice.internal.ConfigCache;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author lepdou 2017-05-04
 */
@Service
public class ConfigService {

  @Autowired
  private GrayReleaseRulesHolder grayReleaseRulesHolder;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private ConfigCache configCache;


  public Release loadConfig(String sourceAppId, String clientIp, String targetAppId, String targetCluster,
                            String namespace, String dataCenter) {
    //load from specified cluster first
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, targetCluster)) {
      Release clusterRelease = findRelease(sourceAppId, clientIp, targetAppId, targetCluster, namespace);

      if (!Objects.isNull(clusterRelease)) {
        return clusterRelease;
      }
    }

    //try to load via data center
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, targetCluster)) {
      Release dataCenterRelease = findRelease(sourceAppId, clientIp, targetAppId, dataCenter, namespace);
      if (!Objects.isNull(dataCenterRelease)) {
        return dataCenterRelease;
      }
    }

    //fallback to default release
    return findRelease(sourceAppId, clientIp, targetAppId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
  }

  private Release findRelease(String clientAppId, String clientIp, String configAppId, String
      configClusterName, String configNamespace) {
    Long grayReleaseId = grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(clientAppId, clientIp, configAppId,
                                                                                 configClusterName, configNamespace);

    Release release = null;
    //gray release
    if (grayReleaseId != null) {
      release = releaseService.findActiveOne(grayReleaseId);
    }
    //load from cache
    if (release == null) {
      release = configCache.get(configAppId, configClusterName, configNamespace);
    }

    return release;
  }

}
