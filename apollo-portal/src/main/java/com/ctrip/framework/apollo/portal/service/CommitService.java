package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitService {


  private final AdminServiceAPI.CommitAPI commitAPI;

  public CommitService(final AdminServiceAPI.CommitAPI commitAPI) {
    this.commitAPI = commitAPI;
  }

  public List<CommitDTO> find(String appId, String env, String clusterName, String namespaceName, int page, int size) {
    return commitAPI.find(appId, env, clusterName, namespaceName, page, size);
  }

}
