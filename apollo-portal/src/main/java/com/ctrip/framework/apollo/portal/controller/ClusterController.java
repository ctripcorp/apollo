package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.service.ClusterService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class ClusterController {

  @Autowired
  private ClusterService clusterService;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @PreAuthorize(value = "@permissionValidator.hasCreateClusterPermission(#appId)")
  @PostMapping(value = "apps/{appId}/envs/{env}/clusters")
  public ClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
                                  @Valid @RequestBody ClusterDTO cluster) {

    checkModel(Objects.nonNull(cluster));

    String operator = userInfoHolder.getUser().getUserId();
    cluster.setDataChangeLastModifiedBy(operator);
    cluster.setDataChangeCreatedBy(operator);

    return clusterService.createCluster(Env.valueOf(env), cluster);
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ResponseEntity<Void> deleteCluster(@PathVariable String appId, @PathVariable String env,
                                            @PathVariable String clusterName){
    clusterService.deleteCluster(Env.fromString(env), appId, clusterName);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ClusterDTO loadCluster(@PathVariable("appId") String appId, @PathVariable String env, @PathVariable("clusterName") String clusterName) {

    return clusterService.loadCluster(appId, Env.fromString(env), clusterName);
  }

}
