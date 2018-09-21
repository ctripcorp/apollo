package com.ctrip.framework.apollo.openapi.v1.controller;


import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.listener.ConfigPublishEvent;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController("openapiReleaseController")
@RequestMapping("/openapi/v1/envs/{env}")
public class ReleaseController {

  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private UserService userService;

  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private PortalConfig portalConfig;

  @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", method = RequestMethod.POST)
  public OpenReleaseDTO createRelease(@PathVariable String appId, @PathVariable String env,
                                      @PathVariable String clusterName,
                                      @PathVariable String namespaceName,
                                      @RequestBody NamespaceReleaseModel model,
                                      HttpServletRequest request) {

    checkModel(model != null);
    RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
            .getReleaseTitle()),
        "Params(releaseTitle and releasedBy) can not be empty");

    if (userService.findByUserId(model.getReleasedBy()) == null) {
      throw new BadRequestException("user(releaseBy) not exists");
    }

    model.setAppId(appId);
    model.setEnv(Env.fromString(env).toString());
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);

    return OpenApiBeanUtils.transformFromReleaseDTO(releaseService.publish(model));
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest", method = RequestMethod.GET)
  public OpenReleaseDTO loadLatestActiveRelease(@PathVariable String appId, @PathVariable String env,
                                                @PathVariable String clusterName, @PathVariable
                                                    String namespaceName) {
    ReleaseDTO releaseDTO = releaseService.loadLatestRelease(appId, Env.fromString
        (env), clusterName, namespaceName);
    if (releaseDTO == null) {
      return null;
    }

    return OpenApiBeanUtils.transformFromReleaseDTO(releaseDTO);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/operator/{operator}/releases",
          method = RequestMethod.POST)
  public ReleaseDTO createGrayRelease(@PathVariable String appId,
                                      @PathVariable String env, @PathVariable String clusterName,
                                      @PathVariable String namespaceName, @PathVariable String branchName,
                                      @PathVariable String operator,
                                      HttpServletRequest request) {

    NamespaceReleaseModel model = new NamespaceReleaseModel();
    model.setAppId(appId);
    model.setEnv(env.toUpperCase());
    model.setClusterName(branchName);
    model.setNamespaceName(namespaceName);
    model.setReleasedBy(operator);

    if (model.isEmergencyPublish() && !portalConfig.isEmergencyPublishAllowed(Env.valueOf(env.toUpperCase()))) {
      throw new BadRequestException(String.format("Env: %s is not supported emergency publish now", env));
    }

    ReleaseDTO createdRelease = releaseService.publish(model);

    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId)
            .withCluster(clusterName)
            .withNamespace(namespaceName)
            .withReleaseId(createdRelease.getId())
            .setGrayPublishEvent(true)
            .setEnv(Env.valueOf(env.toUpperCase()));

    publisher.publishEvent(event);

    return createdRelease;
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/operator/{operator}/gray-del-releases",
          method = RequestMethod.POST)
  public ReleaseDTO createGrayDelRelease(@PathVariable String appId,
                                         @PathVariable String env, @PathVariable String clusterName,
                                         @PathVariable String namespaceName, @PathVariable String branchName,
                                         @PathVariable String operator,
                                         @RequestBody NamespaceGrayDelReleaseModel model,
                                         HttpServletRequest request) {

    checkModel(model != null);
    model.setAppId(appId);
    model.setEnv(env.toUpperCase());
    model.setClusterName(branchName);
    model.setNamespaceName(namespaceName);

    if (model.isEmergencyPublish() && !portalConfig.isEmergencyPublishAllowed(Env.valueOf(env.toUpperCase()))) {
      throw new BadRequestException(String.format("Env: %s is not supported emergency publish now", env));
    }

    ReleaseDTO createdRelease = releaseService.publish(model, operator);

    ConfigPublishEvent event = ConfigPublishEvent.instance();
    event.withAppId(appId)
            .withCluster(clusterName)
            .withNamespace(namespaceName)
            .withReleaseId(createdRelease.getId())
            .setGrayPublishEvent(true)
            .setEnv(Env.valueOf(env.toUpperCase()));

    publisher.publishEvent(event);

    return createdRelease;
  }

}
