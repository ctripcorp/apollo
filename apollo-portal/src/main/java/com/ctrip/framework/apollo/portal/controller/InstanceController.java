package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.constants.Env;
import com.ctrip.framework.apollo.portal.entity.vo.Number;
import com.ctrip.framework.apollo.portal.service.InstanceService;
import com.google.common.base.Splitter;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class InstanceController {

    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
        .trimResults();

    private final InstanceService instanceService;

    public InstanceController(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @GetMapping("/envs/{env}/instances/by-release")
    public PageDTO<InstanceDTO> getByRelease(@PathVariable String env, @RequestParam long releaseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {

        return instanceService.getByRelease(Env.valueOf(env), releaseId, page, size);
    }

    @GetMapping("/envs/{env}/instances/by-namespace")
    public PageDTO<InstanceDTO> getByNamespace(@PathVariable String env, @RequestParam String appId,
                                               @RequestParam String clusterName, @RequestParam String namespaceName,
                                               @RequestParam(required = false) String instanceAppId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {

        return instanceService.getByNamespace(Env.valueOf(env), appId, clusterName, namespaceName, instanceAppId, page, size);
    }

    @GetMapping("/envs/{env}/instances/by-namespace/count")
    public ResponseEntity<Number> getInstanceCountByNamespace(@PathVariable String env, @RequestParam String appId,
                                                              @RequestParam String clusterName,
                                                              @RequestParam String namespaceName) {

        int count = instanceService.getInstanceCountByNamepsace(appId, Env.valueOf(env), clusterName, namespaceName);
        return ResponseEntity.ok(new Number(count));
    }

    @GetMapping("/envs/{env}/instances/by-namespace-and-releases-not-in")
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
