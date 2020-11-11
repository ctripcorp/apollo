package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.environment.Env;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 提交Service
 */
@Service
public class CommitService {


  private final AdminServiceAPI.CommitAPI commitAPI;

  public CommitService(final AdminServiceAPI.CommitAPI commitAPI) {
    this.commitAPI = commitAPI;
  }

  /**
   * 获取提交信息列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 提交信息列表
   */
  public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName,
      int page, int size) {
    return commitAPI.find(appId, env, clusterName, namespaceName, page, size);
  }

}
