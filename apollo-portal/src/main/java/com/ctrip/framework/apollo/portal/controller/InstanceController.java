package com.ctrip.framework.apollo.portal.controller;

import com.google.common.base.Splitter;

import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.vo.Number;
import com.ctrip.framework.apollo.portal.service.InstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class InstanceController {

    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
        .trimResults();

    @Autowired
    private InstanceService instanceService;

    @RequestMapping(value = "/envs/{env}/instances/by-release", method = RequestMethod.GET)
    public PageDTO<InstanceDTO> getByRelease(@PathVariable String env, @RequestParam long releaseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        return instanceService.getByRelease(Env.valueOf(env), releaseId, page, size);
    }

    @RequestMapping(value = "/envs/{env}/instances/by-namespace", method = RequestMethod.GET)
    public PageDTO<InstanceDTO> getByNamespace(@PathVariable String env, @RequestParam String appId,
                                               @RequestParam String clusterName, @RequestParam String namespaceName,
                                               @RequestParam(required = false) String instanceAppId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {

        return instanceService.getByNamespace(Env.valueOf(env), appId, clusterName, namespaceName, instanceAppId, page, size);
    }

    @RequestMapping(value = "/envs/{env}/instances/by-namespace/count", method = RequestMethod.GET)
    public ResponseEntity<Number> getInstanceCountByNamespace(@PathVariable String env, @RequestParam String appId,
                                                              @RequestParam String clusterName,
                                                              @RequestParam String namespaceName) {

        int count = instanceService.getInstanceCountByNamepsace(appId, Env.valueOf(env), clusterName, namespaceName);
        return ResponseEntity.ok(new Number(count));
    }

    @RequestMapping(value = "/envs/{env}/instances/by-namespace-and-releases-not-in", method = RequestMethod.GET)
    public List<InstanceDTO> getByReleasesNotIn(@PathVariable String env, @RequestParam String appId,
                                                @RequestParam String clusterName, @RequestParam String namespaceName,
                                                @RequestParam String releaseIds) {

        Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
            .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(releaseIdSet)) {
            throw new BadRequestException("release ids can not be empty");
        }

        return instanceService.getByReleasesNotIn(Env.valueOf(env), appId, clusterName, namespaceName, releaseIdSet);
    }


}
