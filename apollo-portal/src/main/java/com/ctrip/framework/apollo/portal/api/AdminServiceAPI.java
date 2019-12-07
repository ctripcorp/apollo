package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.common.dto.*;
import com.google.common.base.Joiner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;


@Service
public class AdminServiceAPI {

  @Service
  public static class HealthAPI extends API {

    public Health health(String env) {
      return restTemplate.get(env, "/health", Health.class);
    }
  }

  @Service
  public static class AppAPI extends API {

    public AppDTO loadApp(String env, String appId) {
      return restTemplate.get(env, "apps/{appId}", AppDTO.class, appId);
    }

    public AppDTO createApp(String env, AppDTO app) {
      return restTemplate.post(env, "apps", app, AppDTO.class);
    }

    public void updateApp(String env, AppDTO app) {
      restTemplate.put(env, "apps/{appId}", app, app.getAppId());
    }

    public void deleteApp(String env, String appId, String operator) {
      restTemplate.delete(env, "/apps/{appId}?operator={operator}", appId, operator);
    }
  }


  @Service
  public static class NamespaceAPI extends API {

    private ParameterizedTypeReference<Map<String, Boolean>>
        typeReference = new ParameterizedTypeReference<Map<String, Boolean>>() {
    };

    public List<NamespaceDTO> findNamespaceByCluster(String appId, String env, String clusterName) {
      NamespaceDTO[] namespaceDTOs = restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces",
          NamespaceDTO[].class, appId,
          clusterName);
      return Arrays.asList(namespaceDTOs);
    }

    public NamespaceDTO loadNamespace(String appId, String env, String clusterName,
        String namespaceName) {
      return
          restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}",
              NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    public NamespaceDTO findPublicNamespaceForAssociatedNamespace(String env, String appId, String clusterName,
        String namespaceName) {
      return
          restTemplate
              .get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
                  NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    public NamespaceDTO createNamespace(String env, NamespaceDTO namespace) {
      return restTemplate
          .post(env, "apps/{appId}/clusters/{clusterName}/namespaces", namespace, NamespaceDTO.class,
              namespace.getAppId(), namespace.getClusterName());
    }

    public AppNamespaceDTO createAppNamespace(String env, AppNamespaceDTO appNamespace) {
      return restTemplate
          .post(env, "apps/{appId}/appnamespaces", appNamespace, AppNamespaceDTO.class, appNamespace.getAppId());
    }

    public AppNamespaceDTO createMissingAppNamespace(String env, AppNamespaceDTO appNamespace) {
      return restTemplate
          .post(env, "apps/{appId}/appnamespaces?silentCreation=true", appNamespace, AppNamespaceDTO.class,
              appNamespace.getAppId());
    }

    public List<AppNamespaceDTO> getAppNamespaces(String appId, String env) {
      AppNamespaceDTO[] appNamespaceDTOs = restTemplate.get(env, "apps/{appId}/appnamespaces", AppNamespaceDTO[].class, appId);
      return Arrays.asList(appNamespaceDTOs);
    }

    public void deleteNamespace(String env, String appId, String clusterName, String namespaceName, String operator) {
      restTemplate
          .delete(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}?operator={operator}", appId,
              clusterName,
              namespaceName, operator);
    }

    public Map<String, Boolean> getNamespacePublishInfo(String env, String appId) {
      return restTemplate.get(env, "apps/{appId}/namespaces/publish_info", typeReference, appId).getBody();
    }

    public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(String env, String publicNamespaceName,
        int page, int size) {
      NamespaceDTO[] namespaceDTOs =
          restTemplate.get(env, "/appnamespaces/{publicNamespaceName}/namespaces?page={page}&size={size}",
              NamespaceDTO[].class, publicNamespaceName, page, size);
      return Arrays.asList(namespaceDTOs);
    }

    public int countPublicAppNamespaceAssociatedNamespaces(String env, String publicNamesapceName) {
      Integer count =
          restTemplate.get(env, "/appnamespaces/{publicNamespaceName}/associated-namespaces/count", Integer.class,
              publicNamesapceName);

      return count == null ? 0 : count;
    }

    public void deleteAppNamespace(String env, String appId, String namespaceName, String operator) {
      restTemplate.delete(env, "/apps/{appId}/appnamespaces/{namespaceName}?operator={operator}", appId, namespaceName,
          operator);
    }
  }

  @Service
  public static class ItemAPI extends API {

    public List<ItemDTO> findItems(String appId, String env, String clusterName, String namespaceName) {
      ItemDTO[] itemDTOs =
          restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
              ItemDTO[].class, appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    public List<ItemDTO> findDeletedItems(String appId, String env, String clusterName, String namespaceName) {
      ItemDTO[] itemDTOs =
          restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/deleted",
              ItemDTO[].class, appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    public ItemDTO loadItem(String env, String appId, String clusterName, String namespaceName, String key) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key}",
          ItemDTO.class, appId, clusterName, namespaceName, key);
    }

    public ItemDTO loadItemById(String env, long itemId) {
      return restTemplate.get(env, "items/{itemId}", ItemDTO.class, itemId);
    }

    public void updateItemsByChangeSet(String appId, String env, String clusterName, String namespace,
        ItemChangeSets changeSets) {
      restTemplate.post(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
          changeSets, Void.class, appId, clusterName, namespace);
    }

    public void updateItem(String appId, String env, String clusterName, String namespace, long itemId, ItemDTO item) {
      restTemplate.put(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}",
          item, appId, clusterName, namespace, itemId);

    }

    public ItemDTO createItem(String appId, String env, String clusterName, String namespace, ItemDTO item) {
      return restTemplate.post(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
          item, ItemDTO.class, appId, clusterName, namespace);
    }

    public void deleteItem(String env, long itemId, String operator) {

      restTemplate.delete(env, "items/{itemId}?operator={operator}", itemId, operator);
    }
  }

  @Service
  public static class ClusterAPI extends API {

    public List<ClusterDTO> findClustersByApp(String appId, String env) {
      ClusterDTO[] clusterDTOs = restTemplate.get(env, "apps/{appId}/clusters", ClusterDTO[].class,
          appId);
      return Arrays.asList(clusterDTOs);
    }

    public ClusterDTO loadCluster(String appId, String env, String clusterName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}", ClusterDTO.class,
          appId, clusterName);
    }

    public boolean isClusterUnique(String appId, String env, String clusterName) {
      return restTemplate
          .get(env, "apps/{appId}/cluster/{clusterName}/unique", Boolean.class,
              appId, clusterName);

    }

    public ClusterDTO create(String env, ClusterDTO cluster) {
      return restTemplate.post(env, "apps/{appId}/clusters", cluster, ClusterDTO.class,
          cluster.getAppId());
    }


    public void delete(String env, String appId, String clusterName, String operator) {
      restTemplate.delete(env, "apps/{appId}/clusters/{clusterName}?operator={operator}", appId, clusterName, operator);
    }
  }

  @Service
  public static class ReleaseAPI extends API {

    private static final Joiner JOINER = Joiner.on(",");

    public ReleaseDTO loadRelease(String env, long releaseId) {
      return restTemplate.get(env, "releases/{releaseId}", ReleaseDTO.class, releaseId);
    }

    public List<ReleaseDTO> findReleaseByIds(String env, Set<Long> releaseIds) {
      if (CollectionUtils.isEmpty(releaseIds)) {
        return Collections.emptyList();
      }

      ReleaseDTO[]
          releases =
          restTemplate.get(env, "/releases?releaseIds={releaseIds}", ReleaseDTO[].class, JOINER.join(releaseIds));
      return Arrays.asList(releases);

    }

    public List<ReleaseDTO> findAllReleases(String appId, String env, String clusterName, String namespaceName, int page,
        int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all?page={page}&size={size}",
          ReleaseDTO[].class,
          appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    public List<ReleaseDTO> findActiveReleases(String appId, String env, String clusterName, String namespaceName,
        int page,
        int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active?page={page}&size={size}",
          ReleaseDTO[].class,
          appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    public ReleaseDTO loadLatestRelease(String appId, String env, String clusterName,
        String namespace) {
      ReleaseDTO releaseDTO = restTemplate
          .get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest",
              ReleaseDTO.class, appId, clusterName, namespace);
      return releaseDTO;
    }

    public ReleaseDTO createRelease(String appId, String env, String clusterName, String namespace,
        String releaseName, String releaseComment, String operator,
        boolean isEmergencyPublish) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("name", releaseName);
      parameters.add("comment", releaseComment);
      parameters.add("operator", operator);
      parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
      HttpEntity<MultiValueMap<String, String>> entity =
          new HttpEntity<>(parameters, headers);
      ReleaseDTO response = restTemplate.post(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", entity,
          ReleaseDTO.class, appId, clusterName, namespace);
      return response;
    }

    public ReleaseDTO createGrayDeletionRelease(String appId, String env, String clusterName, String namespace,
                                    String releaseName, String releaseComment, String operator,
                                    boolean isEmergencyPublish, Set<String> grayDelKeys) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("releaseName", releaseName);
      parameters.add("comment", releaseComment);
      parameters.add("operator", operator);
      parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
      grayDelKeys.forEach(key -> parameters.add("grayDelKeys",key));
      HttpEntity<MultiValueMap<String, String>> entity =
              new HttpEntity<>(parameters, headers);
      ReleaseDTO response = restTemplate.post(
              env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/gray-del-releases", entity,
              ReleaseDTO.class, appId, clusterName, namespace);
      return response;
    }

    public ReleaseDTO updateAndPublish(String appId, String env, String clusterName, String namespace,
        String releaseName, String releaseComment, String branchName,
        boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSets changeSets) {

      return restTemplate.post(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/updateAndPublish?"
              + "releaseName={releaseName}&releaseComment={releaseComment}&branchName={branchName}"
              + "&deleteBranch={deleteBranch}&isEmergencyPublish={isEmergencyPublish}",
          changeSets, ReleaseDTO.class, appId, clusterName, namespace,
          releaseName, releaseComment, branchName, deleteBranch, isEmergencyPublish);

    }

    public void rollback(String env, long releaseId, String operator) {
      restTemplate.put(env,
          "releases/{releaseId}/rollback?operator={operator}",
          null, releaseId, operator);
    }
  }

  @Service
  public static class CommitAPI extends API {

    public List<CommitDTO> find(String appId, String env, String clusterName, String namespaceName, int page, int size) {

      CommitDTO[] commitDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit?page={page}&size={size}",
          CommitDTO[].class,
          appId, clusterName, namespaceName, page, size);

      return Arrays.asList(commitDTOs);
    }
  }

  @Service
  public static class NamespaceLockAPI extends API {

    public NamespaceLockDTO getNamespaceLockOwner(String appId, String env, String clusterName, String namespaceName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock",
          NamespaceLockDTO.class,
          appId, clusterName, namespaceName);

    }
  }

  @Service
  public static class InstanceAPI extends API {

    private Joiner joiner = Joiner.on(",");
    private ParameterizedTypeReference<PageDTO<InstanceDTO>>
        pageInstanceDtoType =
        new ParameterizedTypeReference<PageDTO<InstanceDTO>>() {
        };

    public PageDTO<InstanceDTO> getByRelease(String env, long releaseId, int page, int size) {
      ResponseEntity<PageDTO<InstanceDTO>>
          entity =
          restTemplate
              .get(env, "/instances/by-release?releaseId={releaseId}&page={page}&size={size}", pageInstanceDtoType,
                  releaseId, page, size);
      return entity.getBody();

    }

    public List<InstanceDTO> getByReleasesNotIn(String appId, String env, String clusterName, String namespaceName,
        Set<Long> releaseIds) {

      InstanceDTO[]
          instanceDTOs =
          restTemplate.get(env,
              "/instances/by-namespace-and-releases-not-in?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}&releaseIds={releaseIds}",
              InstanceDTO[].class, appId, clusterName, namespaceName, joiner.join(releaseIds));

      return Arrays.asList(instanceDTOs);
    }

    public PageDTO<InstanceDTO> getByNamespace(String appId, String env, String clusterName, String namespaceName,
        String instanceAppId,
        int page, int size) {
      ResponseEntity<PageDTO<InstanceDTO>>
          entity =
          restTemplate.get(env,
              "/instances/by-namespace?appId={appId}"
                  + "&clusterName={clusterName}&namespaceName={namespaceName}&instanceAppId={instanceAppId}"
                  + "&page={page}&size={size}",
              pageInstanceDtoType, appId, clusterName, namespaceName, instanceAppId, page, size);
      return entity.getBody();
    }

    public int getInstanceCountByNamespace(String appId, String env, String clusterName, String namespaceName) {
      Integer
          count =
          restTemplate.get(env,
              "/instances/by-namespace/count?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}",
              Integer.class, appId, clusterName, namespaceName);
      if (count == null) {
        return 0;
      }
      return count;
    }
  }

  @Service
  public static class NamespaceBranchAPI extends API {

    public NamespaceDTO createBranch(String appId, String env, String clusterName,
        String namespaceName, String operator) {
      return restTemplate
          .post(env, "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches?operator={operator}",
              null, NamespaceDTO.class, appId, clusterName, namespaceName, operator);
    }

    public NamespaceDTO findBranch(String appId, String env, String clusterName,
        String namespaceName) {
      return restTemplate.get(env, "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches",
          NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    public GrayReleaseRuleDTO findBranchGrayRules(String appId, String env, String clusterName,
        String namespaceName, String branchName) {
      return restTemplate
          .get(env, "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
              GrayReleaseRuleDTO.class, appId, clusterName, namespaceName, branchName);

    }

    public void updateBranchGrayRules(String appId, String env, String clusterName,
        String namespaceName, String branchName, GrayReleaseRuleDTO rules) {
      restTemplate
          .put(env, "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
              rules, appId, clusterName, namespaceName, branchName);

    }

    public void deleteBranch(String appId, String env, String clusterName,
        String namespaceName, String branchName, String operator) {
      restTemplate.delete(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}?operator={operator}",
          appId, clusterName, namespaceName, branchName, operator);
    }
  }

  @Service
  public static class ReleaseHistoryAPI extends API {

    private ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>> type =
        new ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>>() {
        };


    public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(String appId, String env, String clusterName,
        String namespaceName, int page, int size) {
      return restTemplate.get(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories?page={page}&size={size}",
          type, appId, clusterName, namespaceName, page, size).getBody();
    }

    public PageDTO<ReleaseHistoryDTO> findByReleaseIdAndOperation(String env, long releaseId, int operation, int page,
        int size) {
      return restTemplate.get(env,
          "/releases/histories/by_release_id_and_operation?releaseId={releaseId}&operation={operation}&page={page}&size={size}",
          type, releaseId, operation, page, size).getBody();
    }

    public PageDTO<ReleaseHistoryDTO> findByPreviousReleaseIdAndOperation(String env, long previousReleaseId,
        int operation, int page, int size) {
      return restTemplate.get(env,
          "/releases/histories/by_previous_release_id_and_operation?previousReleaseId={releaseId}&operation={operation}&page={page}&size={size}",
          type, previousReleaseId, operation, page, size).getBody();
    }

  }

}
