package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class NamespaceController {

    private final NamespaceService namespaceService;

    public NamespaceController(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * createNamespace
     * @param appId
     * @param clusterName
     * @param dto
     * @return
     */
    @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces")
    public NamespaceDTO create(@PathVariable("appId") String appId,
                               @PathVariable("clusterName") String clusterName,
                               @Valid @RequestBody NamespaceDTO dto) {
        Namespace entity = BeanUtils.transform(Namespace.class, dto);
        Namespace managedEntity = namespaceService.findOne(appId, clusterName, entity.getNamespaceName());
        if (managedEntity != null) {
            throw new BadRequestException("namespace already exist.");
        }

        entity = namespaceService.save(entity);

        return BeanUtils.transform(NamespaceDTO.class, entity);
    }

    /**
     * deleteNamespace
     * @param appId
     * @param clusterName
     * @param namespaceName
     * @param operator
     */
    @DeleteMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public void delete(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName,
                       @PathVariable("namespaceName") String namespaceName, @RequestParam String operator) {
        Namespace entity = namespaceService.findOne(appId, clusterName, namespaceName);
        if (entity == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));

        namespaceService.deleteNamespace(entity, operator);
    }

    /**
     * findNamespaceByCluster 方法
     *
     * @param appId
     * @param clusterName
     * @return
     */
    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces")
    public List<NamespaceDTO> find(@PathVariable("appId") String appId,
                                   @PathVariable("clusterName") String clusterName) {
        //apollo中namespace表中记载
        List<Namespace> groups = namespaceService.findNamespaces(appId, clusterName);
        return BeanUtils.batchTransform(NamespaceDTO.class, groups);
    }

    /**
     * loadNamespace
     * @param namespaceId
     * @return
     */
    @GetMapping("/namespaces/{namespaceId}")
    public NamespaceDTO get(@PathVariable("namespaceId") Long namespaceId) {
        Namespace namespace = namespaceService.findOne(namespaceId);
        if (namespace == null)
            throw new NotFoundException(String.format("namespace not found for %s", namespaceId));
        return BeanUtils.transform(NamespaceDTO.class, namespace);
    }

    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}")
    public NamespaceDTO get(@PathVariable("appId") String appId,
                            @PathVariable("clusterName") String clusterName,
                            @PathVariable("namespaceName") String namespaceName) {
        Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
        return BeanUtils.transform(NamespaceDTO.class, namespace);
    }

    /**
     * findPublicNamespaceForAssociatedNamespace
     * 查找关联命名空间的公共命名空间
     * @param appId
     * @param clusterName
     * @param namespaceName
     * @return
     */
    @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace")
    public NamespaceDTO findPublicNamespaceForAssociatedNamespace(@PathVariable String appId,
                                                                  @PathVariable String clusterName,
                                                                  @PathVariable String namespaceName) {
        Namespace namespace = namespaceService.findPublicNamespaceForAssociatedNamespace(clusterName, namespaceName);

        if (namespace == null) {
            throw new NotFoundException(String.format("public namespace not found. namespace:%s", namespaceName));
        }

        return BeanUtils.transform(NamespaceDTO.class, namespace);
    }

    /**
     * 查询这个namespace的所有集群所有项目的发布状态，如果存在没有纺布的属性，那么就是为发布的状态
     * cluster -> cluster has not published namespaces?
     */
    @GetMapping("/apps/{appId}/namespaces/publish_info")
    public Map<String, Boolean> namespacePublishInfo(@PathVariable String appId) {
        return namespaceService.namespacePublishInfo(appId);
    }


}
