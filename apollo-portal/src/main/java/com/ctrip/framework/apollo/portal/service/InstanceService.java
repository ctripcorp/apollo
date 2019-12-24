package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class InstanceService {


  private final AdminServiceAPI.InstanceAPI instanceAPI;

  public InstanceService(final AdminServiceAPI.InstanceAPI instanceAPI) {
    this.instanceAPI = instanceAPI;
  }

  public PageDTO<InstanceDTO> getByRelease(Env env, long releaseId, int page, int size){
    return instanceAPI.getByRelease(env, releaseId, page, size);
  }

  public PageDTO<InstanceDTO> getByNamespace(Env env, String appId, String clusterName, String namespaceName,
                                             String instanceAppId, int page, int size){
    return instanceAPI.getByNamespace(appId, env, clusterName, namespaceName, instanceAppId, page, size);
  }

  public int getInstanceCountByNamepsace(String appId, Env env, String clusterName, String namespaceName){
    return instanceAPI.getInstanceCountByNamespace(appId, env, clusterName, namespaceName);
  }

  public List<InstanceDTO> getByReleasesNotIn(Env env, String appId, String clusterName, String namespaceName, Set<Long> releaseIds){
    return instanceAPI.getByReleasesNotIn(appId, env, clusterName, namespaceName, releaseIds);
  }



}
