package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.service.CommitService;
import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提交记录 Controller层
 */
@RestController
public class CommitController {

  private final CommitService commitService;

  public CommitController(final CommitService commitService) {
    this.commitService = commitService;
  }

  /**
   * 查询提交记录
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 提交记录列表信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit")
  public List<CommitDTO> find(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespaceName, Pageable pageable) {

    List<Commit> commits = commitService.find(appId, clusterName, namespaceName, pageable);
    return BeanUtils.batchTransform(CommitDTO.class, commits);
  }

}
