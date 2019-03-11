package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(final ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    /**
     * 创建
     * @param appId
     * @param autoCreatePrivateNamespace
     * @param dto
     * @return
     */
    @PostMapping("/apps/{appId}/clusters")
    public ClusterDTO create(@PathVariable("appId") String appId,
                             @RequestParam(value = "autoCreatePrivateNamespace", defaultValue = "true") boolean autoCreatePrivateNamespace,
                             @Valid @RequestBody ClusterDTO dto) {
        Cluster entity = BeanUtils.transform(Cluster.class, dto);

        Cluster managedEntity = clusterService.findOne(appId, entity.getName());

        if (managedEntity != null) {
            throw new BadRequestException("cluster already exist.");
        }

        if (autoCreatePrivateNamespace) {//TODO 默认就是true 没发现那里使用false
            entity = clusterService.saveWithInstanceOfAppNamespaces(entity);
        } else {
            entity = clusterService.saveWithoutInstanceOfAppNamespaces(entity);
        }

        return BeanUtils.transform(ClusterDTO.class, entity);
    }

    @DeleteMapping("/apps/{appId}/clusters/{clusterName:.+}")
    public void delete(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName, @RequestParam String operator) {

        Cluster entity = clusterService.findOne(appId, clusterName);

        if (entity == null) {
            throw new NotFoundException("cluster not found for clusterName " + clusterName);
        }

        if (ConfigConsts.CLUSTER_NAME_DEFAULT.equals(entity.getName())) {
            throw new BadRequestException("can not delete default cluster!");
        }

        clusterService.delete(entity.getId(), operator);
    }

    /**
     * findClustersByApp
     *
     * @param appId
     * @return
     */
    @GetMapping("/apps/{appId}/clusters")
    public List<ClusterDTO> find(@PathVariable("appId") String appId) {
        List<Cluster> clusters = clusterService.findParentClusters(appId);
        return BeanUtils.batchTransform(ClusterDTO.class, clusters);
    }

    /**
     * loadCluster
     *
     * @param appId
     * @param clusterName
     * @return
     */
    @GetMapping("/apps/{appId}/clusters/{clusterName:.+}")
    public ClusterDTO get(@PathVariable("appId") String appId,
                          @PathVariable("clusterName") String clusterName) {
        Cluster cluster = clusterService.findOne(appId, clusterName);
        if (cluster == null) {
            throw new NotFoundException("cluster not found for name " + clusterName);
        }
        return BeanUtils.transform(ClusterDTO.class, cluster);
    }

    /**
     * isClusterUnique
     *
     * @param appId
     * @param clusterName
     * @return
     */
    @GetMapping("/apps/{appId}/cluster/{clusterName}/unique")
    public boolean isAppIdUnique(@PathVariable("appId") String appId,
                                 @PathVariable("clusterName") String clusterName) {
        return clusterService.isClusterNameUnique(appId, clusterName);
    }
}
