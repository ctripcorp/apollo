package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseBO;
import com.ctrip.framework.apollo.portal.listener.ConfigPublishEvent;
import com.ctrip.framework.apollo.portal.service.ReleaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class ReleaseController {

  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private ApplicationEventPublisher publisher;

  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases", method = RequestMethod.POST)
  public ReleaseDTO createRelease(@PathVariable String appId,
                                  @PathVariable String env, @PathVariable String clusterName,
                                  @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {

    checkModel(model != null);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);

    ReleaseDTO createdRelease = releaseService.publish(model);

    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId)
        .withCluster(clusterName)
        .withNamespace(namespaceName)
        .withReleaseId(createdRelease.getId())
        .setNormalPublishEvent(true)
        .setEnv(Env.valueOf(env));

    publisher.publishEvent(event);

    return createdRelease;
  }

  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/releases",
      method = RequestMethod.POST)
  public ReleaseDTO createGrayRelease(@PathVariable String appId,
                                      @PathVariable String env, @PathVariable String clusterName,
                                      @PathVariable String namespaceName, @PathVariable String branchName,
                                      @RequestBody NamespaceReleaseModel model) {

    checkModel(model != null);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(branchName);
    model.setNamespaceName(namespaceName);

    ReleaseDTO createdRelease = releaseService.publish(model);

    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId)
        .withCluster(clusterName)
        .withNamespace(namespaceName)
        .withReleaseId(createdRelease.getId())
        .setGrayPublishEvent(true)
        .setEnv(Env.valueOf(env));

    publisher.publishEvent(event);

    return createdRelease;
  }


  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all")
  public List<ReleaseBO> findAllReleases(@PathVariable String appId,
                                         @PathVariable String env,
                                         @PathVariable String clusterName,
                                         @PathVariable String namespaceName,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int size) {

    RequestPrecondition.checkNumberPositive(size);
    RequestPrecondition.checkNumberNotNegative(page);

    return releaseService.findAllReleases(appId, Env.valueOf(env), clusterName, namespaceName, page, size);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
  public List<ReleaseDTO> findActiveReleases(@PathVariable String appId,
                                             @PathVariable String env,
                                             @PathVariable String clusterName,
                                             @PathVariable String namespaceName,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "5") int size) {

    RequestPrecondition.checkNumberPositive(size);
    RequestPrecondition.checkNumberNotNegative(page);

    return releaseService.findActiveReleases(appId, Env.valueOf(env), clusterName, namespaceName, page, size);
  }

  @RequestMapping(value = "/envs/{env}/releases/compare")
  public ReleaseCompareResult compareRelease(@PathVariable String env,
                                             @RequestParam long baseReleaseId,
                                             @RequestParam long toCompareReleaseId) {

    return releaseService.compare(Env.valueOf(env), baseReleaseId, toCompareReleaseId);
  }


  @RequestMapping(path = "/envs/{env}/releases/{releaseId}/rollback", method = RequestMethod.PUT)
  public void rollback(@PathVariable String env,
                       @PathVariable long releaseId) {
    releaseService.rollback(Env.valueOf(env), releaseId);
    ReleaseDTO release = releaseService.findReleaseById(Env.valueOf(env), releaseId);
    if (release == null) {
      return;
    }

    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(release.getAppId())
        .withCluster(release.getClusterName())
        .withNamespace(release.getNamespaceName())
        .withPreviousReleaseId(releaseId)
        .setRollbackEvent(true)
        .setEnv(Env.valueOf(env));

    publisher.publishEvent(event);
  }
}
