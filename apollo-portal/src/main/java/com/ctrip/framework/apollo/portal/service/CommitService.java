package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitService {


  private final AdminServiceAPI.CommitAPI commitAPI;
  private final AdditionalUserInfoEnrichService additionalUserInfoEnrichService;

  public CommitService(final AdminServiceAPI.CommitAPI commitAPI,
      AdditionalUserInfoEnrichService additionalUserInfoEnrichService) {
    this.commitAPI = commitAPI;
    this.additionalUserInfoEnrichService = additionalUserInfoEnrichService;
  }

  public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName, int page, int size) {
    List<CommitDTO> dtoList = commitAPI.find(appId, env, clusterName, namespaceName, page, size);
    this.additionalUserInfoEnrichService.enrichAdditionalUserInfo(dtoList);
    return dtoList;
  }

}
