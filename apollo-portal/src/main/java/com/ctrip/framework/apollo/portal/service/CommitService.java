package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.utils.PreferredUsernameUtil;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.CommitAPI;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.spi.UserService;
import java.util.Collections;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.util.CollectionUtils;

@Service
public class CommitService {


  private final AdminServiceAPI.CommitAPI commitAPI;
  private final UserService userService;

  public CommitService(final CommitAPI commitAPI,
      final UserService userService) {
    this.commitAPI = commitAPI;
    this.userService = userService;
  }

  public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName, int page, int size) {
    List<CommitDTO> dtoList = commitAPI.find(appId, env, clusterName, namespaceName, page, size);
    if (CollectionUtils.isEmpty(dtoList)) {
      return Collections.emptyList();
    }
    PreferredUsernameUtil.enrichPreferredUserName(dtoList, this.userService::findPreferredUsernameMapByUserIds);
    return dtoList;
  }

}
