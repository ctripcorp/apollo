package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.service.CommitService;
import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class CommitController {

  @Autowired
  private CommitService commitService;

  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit")
  public List<CommitDTO> find(@PathVariable String appId, @PathVariable String clusterName,
                              @PathVariable String namespaceName, Pageable pageable){

    List<Commit> commits = commitService.find(appId, clusterName, namespaceName, pageable);
    return BeanUtils.batchTransform(CommitDTO.class, commits);
  }

}
