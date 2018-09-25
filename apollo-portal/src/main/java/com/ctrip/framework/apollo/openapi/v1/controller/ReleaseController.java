package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.dto.NamespaceGrayDelReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.listener.ConfigPublishEvent;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.spi.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController("openapiReleaseController")
@RequestMapping("/openapi/v1/envs/{env}")
public class ReleaseController {

    @Autowired
    private ReleaseService releaseService;
    @Autowired
    private UserService userService;

    @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", method = RequestMethod.POST)
    public OpenReleaseDTO createRelease(@PathVariable String appId, @PathVariable String env,
                                        @PathVariable String clusterName,
                                        @PathVariable String namespaceName,
                                        @RequestBody NamespaceReleaseDTO model,
                                        HttpServletRequest request) {

        checkModel(model != null);
        RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
                        .getReleaseTitle()),
                "Params(releaseTitle and releasedBy) can not be empty");

        if (userService.findByUserId(model.getReleasedBy()) == null) {
            throw new BadRequestException("user(releaseBy) not exists");
        }

        NamespaceReleaseModel releaseModel = BeanUtils.transfrom(NamespaceReleaseModel.class, model);

        releaseModel.setAppId(appId);
        releaseModel.setEnv(Env.fromString(env).toString());
        releaseModel.setClusterName(clusterName);
        releaseModel.setNamespaceName(namespaceName);

        return OpenApiBeanUtils.transformFromReleaseDTO(releaseService.publish(releaseModel));
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

//    @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
//    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/operator/{operator}/releases",
//            method = RequestMethod.POST)
//    public ReleaseDTO createGrayRelease(@PathVariable String appId,
//                                        @PathVariable String env, @PathVariable String clusterName,
//                                        @PathVariable String namespaceName, @PathVariable String branchName,
//                                        @RequestBody NamespaceReleaseDTO model,
//                                        HttpServletRequest request) {
//
//        NamespaceReleaseModel model = new NamespaceReleaseModel();
//        model.setAppId(appId);
//        model.setEnv(env.toUpperCase());
//        model.setClusterName(branchName);
//        model.setNamespaceName(namespaceName);
//        model.setReleasedBy(operator);
//
//        if (model.isEmergencyPublish() && !portalConfig.isEmergencyPublishAllowed(Env.valueOf(env.toUpperCase()))) {
//            throw new BadRequestException(String.format("Env: %s is not supported emergency publish now", env));
//        }
//
//        ReleaseDTO createdRelease = releaseService.publish(model);
//
//        ConfigPublishEvent event = ConfigPublishEvent.instance();
//        event.withAppId(appId)
//                .withCluster(clusterName)
//                .withNamespace(namespaceName)
//                .withReleaseId(createdRelease.getId())
//                .setGrayPublishEvent(true)
//                .setEnv(Env.valueOf(env.toUpperCase()));
//
//        publisher.publishEvent(event);
//
//        return createdRelease;
//    }

    @PreAuthorize(value = "@consumerPermissionValidator.hasReleaseNamespacePermission(#request, #appId, #namespaceName, #env)")
    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/gray-del-releases",
            method = RequestMethod.POST)
    public OpenReleaseDTO createGrayDelRelease(@PathVariable String appId,
                                               @PathVariable String env, @PathVariable String clusterName,
                                               @PathVariable String namespaceName, @PathVariable String branchName,
                                               @RequestBody NamespaceGrayDelReleaseDTO model,
                                               HttpServletRequest request) {

        checkModel(model != null);
        RequestPrecondition.checkArguments(!StringUtils.isContainEmpty(model.getReleasedBy(), model
                        .getReleaseTitle()),
                "Params(releaseTitle and releasedBy) can not be empty");
        RequestPrecondition.checkArguments(model.getGrayDelKeys() != null,
                "Params(grayDelKeys) can not be null");

        if (userService.findByUserId(model.getReleasedBy()) == null) {
            throw new BadRequestException("user(releaseBy) not exists");
        }

        NamespaceGrayDelReleaseModel releaseModel = BeanUtils.transfrom(NamespaceGrayDelReleaseModel.class, model);
        releaseModel.setAppId(appId);
        releaseModel.setEnv(env.toUpperCase());
        releaseModel.setClusterName(branchName);
        releaseModel.setNamespaceName(namespaceName);

        return OpenApiBeanUtils.transformFromReleaseDTO(releaseService.publish(releaseModel, releaseModel.getReleasedBy()));
    }

}
