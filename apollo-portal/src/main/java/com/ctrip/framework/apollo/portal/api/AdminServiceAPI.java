package com.ctrip.framework.apollo.portal.api;

import com.ctrip.framework.apollo.common.dto.AccessKeyDTO;
import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseHistoryDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 * 管理服务API
 */
@Service
public class AdminServiceAPI {

  /**
   * 健康api
   */
  @Service
  public static class HealthAPI extends API {

    /**
     * health检查
     *
     * @param env 环境
     * @return 健康情况
     */
    public Health health(Env env) {
      return restTemplate.get(env, "/health", Health.class);
    }
  }

  /**
   * 应用api
   */
  @Service
  public static class AppAPI extends API {

    /**
     * 加载应用信息
     *
     * @param env   环境
     * @param appId 应用id
     * @return 应用信息
     */
    public AppDTO loadApp(Env env, String appId) {
      return restTemplate.get(env, "apps/{appId}", AppDTO.class, appId);
    }

    /**
     * 创建应用
     *
     * @param env 环境
     * @param app 应用信息DTO
     * @return 创建的应用信息
     */
    public AppDTO createApp(Env env, AppDTO app) {
      return restTemplate.post(env, "apps", app, AppDTO.class);
    }

    /**
     * 更新应用信息
     *
     * @param env 环境
     * @param app 应用信息DTO
     */
    public void updateApp(Env env, AppDTO app) {
      restTemplate.put(env, "apps/{appId}", app, app.getAppId());
    }

    /**
     * 删除应用
     *
     * @param env      环境
     * @param appId    应用id
     * @param operator 操作人
     */
    public void deleteApp(Env env, String appId, String operator) {
      restTemplate.delete(env, "/apps/{appId}?operator={operator}", appId, operator);
    }
  }

  /**
   * 名称空间api
   */
  @Service
  public static class NamespaceAPI extends API {

    /**
     * 参数类型引用Map
     */
    private ParameterizedTypeReference<Map<String, Boolean>>
        typeReference = new ParameterizedTypeReference<Map<String, Boolean>>() {
    };

    /**
     * 查询指定应用id指定集群名称的名称空间列表信息
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称空间
     * @return 指定应用id指定集群名称的名称空间列表信息
     */
    public List<NamespaceDTO> findNamespaceByCluster(String appId, Env env, String clusterName) {
      NamespaceDTO[] namespaceDTOs = restTemplate
          .get(env, "apps/{appId}/clusters/{clusterName}/namespaces",
              NamespaceDTO[].class, appId, clusterName);
      return Arrays.asList(namespaceDTOs);
    }


    /**
     * 查询关联的名称空间中的公有名称空间
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 公有的名称空间信息
     */
    public NamespaceDTO loadNamespace(String appId, Env env, String clusterName,
        String namespaceName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}",
          NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    /**
     * 查询关联的名称空间中的公有名称空间
     *
     * @param env           环境
     * @param appId         应用id
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 公有的名称空间信息
     */
    public NamespaceDTO findPublicNamespaceForAssociatedNamespace(Env env, String appId,
        String clusterName, String namespaceName) {
      return restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
          NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    /**
     * 保存名称空间
     *
     * @param env       环境
     * @param namespace 名称空间信息
     * @return 保存后的名称空间信息
     */
    public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
      return restTemplate.post(env, "apps/{appId}/clusters/{clusterName}/namespaces", namespace,
          NamespaceDTO.class, namespace.getAppId(), namespace.getClusterName());
    }

    /**
     * 创建应用名称空间
     *
     * @param env          环境
     * @param appNamespace 应用名称空间
     * @return 应用名称空间信息
     */
    public AppNamespaceDTO createAppNamespace(Env env, AppNamespaceDTO appNamespace) {
      return restTemplate.post(env, "apps/{appId}/appnamespaces", appNamespace,
          AppNamespaceDTO.class, appNamespace.getAppId());
    }

    /**
     * 创建应用名称空间
     *
     * @param env          环境
     * @param appNamespace 应用名称空间
     * @return 应用名称空间信息
     */
    public AppNamespaceDTO createMissingAppNamespace(Env env, AppNamespaceDTO appNamespace) {
      return restTemplate.post(env, "apps/{appId}/appnamespaces?silentCreation=true",
          appNamespace, AppNamespaceDTO.class, appNamespace.getAppId());
    }

    /**
     * 根据应用id查询应用名称空间列表
     *
     * @param appId 应用id
     * @param env   环境
     * @return 应用名称空间列表
     */
    public List<AppNamespaceDTO> getAppNamespaces(String appId, Env env) {
      AppNamespaceDTO[] appNamespaceDTOs = restTemplate
          .get(env, "apps/{appId}/appnamespaces", AppNamespaceDTO[].class, appId);
      return Arrays.asList(appNamespaceDTOs);
    }

    /**
     * 删除名称空间
     *
     * @param env           环境
     * @param appId         应用id
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param operator      操作者
     */
    public void deleteNamespace(Env env, String appId, String clusterName, String namespaceName,
        String operator) {
      restTemplate.delete(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}?operator={operator}",
          appId, clusterName, namespaceName, operator);
    }


    /**
     * 名称空间发布信息(cluster -> cluster有没有发布名称空间)
     *
     * @param env   环境
     * @param appId 应用id
     * @return 名称空间发布信息
     */
    public Map<String, Boolean> getNamespacePublishInfo(Env env, String appId) {
      return restTemplate.get(env, "apps/{appId}/namespaces/publish_info", typeReference, appId)
          .getBody();
    }

    /**
     * 查询公有应用名称空间的所有名称空间
     *
     * @param env                 环境
     * @param publicNamespaceName 公有应用名称空间名称
     * @param page                页码
     * @param size                页面大小
     * @return 名称空间列表
     */
    public List<NamespaceDTO> getPublicAppNamespaceAllNamespaces(Env env,
        String publicNamespaceName,
        int page, int size) {
      NamespaceDTO[] namespaceDTOs = restTemplate.get(env,
          "/appnamespaces/{publicNamespaceName}/namespaces?page={page}&size={size}",
          NamespaceDTO[].class, publicNamespaceName, page, size);
      return Arrays.asList(namespaceDTOs);
    }

    /**
     * 统计指定的公有应用名称空间关联的名称空间数量
     *
     * @param env                 环境
     * @param publicNamesapceName 公有应用名称空间名称
     * @return 指定的公有应用名称空间关联的名称空间数量
     */
    public int countPublicAppNamespaceAssociatedNamespaces(Env env, String publicNamesapceName) {
      Integer count = restTemplate.get(env,
          "/appnamespaces/{publicNamespaceName}/associated-namespaces/count", Integer.class,
          publicNamesapceName);
      return count == null ? 0 : count;
    }

    /**
     * 删除应用名称空间
     *
     * @param env           环境
     * @param appId         应用id
     * @param namespaceName 应用名称空间名称
     * @param operator      操作者
     */
    public void deleteAppNamespace(Env env, String appId, String namespaceName, String operator) {
      restTemplate.delete(env,
          "/apps/{appId}/appnamespaces/{namespaceName}?operator={operator}", appId, namespaceName,
          operator);
    }
  }

  /**
   * 配置项 - API
   */
  @Service
  public static class ItemAPI extends API {

    /**
     * 查询指定名称空间的属性的配置项列表信息（以行号升序）
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 指定名称空间的属性的配置项列表信息（以行号升序）
     */
    public List<ItemDTO> findItems(String appId, Env env, String clusterName,
        String namespaceName) {
      ItemDTO[] itemDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
          ItemDTO[].class, appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    /**
     * 查询已经被删除的配置项信息
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 已经被删除的配置项信息
     */
    public List<ItemDTO> findDeletedItems(String appId, Env env, String clusterName,
        String namespaceName) {
      ItemDTO[] itemDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/deleted",
          ItemDTO[].class, appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    /**
     * 找到定Key的配置项信息
     *
     * @param env           环境
     * @param appId         应用id
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param key           配置项Key
     * @return 指定Key的配置项信息
     */
    public ItemDTO loadItem(Env env, String appId, String clusterName, String namespaceName,
        String key) {
      return restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key}",
          ItemDTO.class, appId, clusterName, namespaceName, key);
    }

    /**
     * 通过配置项id找到配置项信息
     *
     * @param env    环境
     * @param itemId 配置项id
     * @return 指定配置项id的配置项信息
     */
    public ItemDTO loadItemById(Env env, long itemId) {
      return restTemplate.get(env, "items/{itemId}", ItemDTO.class, itemId);
    }

    /**
     * 更新配置项
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称
     * @param namespace   名称空间名称
     * @param changeSets  改变的配置项
     * @return 配置项创建、更新、删除的列表数据
     */
    public void updateItemsByChangeSet(String appId, Env env, String clusterName, String namespace,
        ItemChangeSets changeSets) {
      restTemplate.post(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
          changeSets, Void.class, appId, clusterName, namespace);
    }

    /**
     * 更新配置项
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称
     * @param namespace   名称空间名称
     * @param itemId      配置项id
     * @param item        配置项信息
     * @return 更新后的配置项
     */
    public void updateItem(String appId, Env env, String clusterName, String namespace, long itemId,
        ItemDTO item) {
      restTemplate.put(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}",
          item, appId, clusterName, namespace, itemId);

    }

    /**
     * 添加配置项
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称
     * @param namespace   名称空间名称
     * @param item        配置项信息
     * @return 创建后的配置项信息
     */
    public ItemDTO createItem(String appId, Env env, String clusterName, String namespace,
        ItemDTO item) {
      return restTemplate.post(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
          item, ItemDTO.class, appId, clusterName, namespace);
    }

    /**
     * 删除配置项
     *
     * @param env      环境
     * @param itemId   配置项id
     * @param operator 操作人
     */
    public void deleteItem(Env env, long itemId, String operator) {
      restTemplate.delete(env, "items/{itemId}?operator={operator}", itemId, operator);
    }
  }

  /**
   * 集群 - API
   */
  @Service
  public static class ClusterAPI extends API {

    /**
     * 查询应用下的集群列表
     *
     * @param appId 应用id
     * @param env   环境
     * @return 集群列表
     */
    public List<ClusterDTO> findClustersByApp(String appId, Env env) {
      ClusterDTO[] clusterDTOs = restTemplate.get(env, "apps/{appId}/clusters", ClusterDTO[].class,
          appId);
      return Arrays.asList(clusterDTOs);
    }

    /**
     * 加载集群
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称
     * @return 集群Dto信息
     */
    public ClusterDTO loadCluster(String appId, Env env, String clusterName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}", ClusterDTO.class,
          appId, clusterName);
    }

    /**
     * 集群名称是否唯一
     *
     * @param appId       应用id
     * @param clusterName 名称空间名称
     * @return true, 唯一，否则，false,名称已经存在
     */
    public boolean isClusterUnique(String appId, Env env, String clusterName) {
      return restTemplate.get(env, "apps/{appId}/cluster/{clusterName}/unique", Boolean.class,
          appId, clusterName);

    }

    /**
     * 创建集群
     *
     * @param env     环境
     * @param cluster 集群信息
     * @return 创建的集群信息
     */
    public ClusterDTO create(Env env, ClusterDTO cluster) {
      return restTemplate.post(env, "apps/{appId}/clusters", cluster, ClusterDTO.class,
          cluster.getAppId());
    }

    /**
     * 删除集群信息
     *
     * @param env         环境
     * @param appId       应用id
     * @param clusterName 集群名称
     * @param operator    操作者
     */

    public void delete(Env env, String appId, String clusterName, String operator) {
      restTemplate.delete(env, "apps/{appId}/clusters/{clusterName}?operator={operator}", appId,
          clusterName, operator);
    }
  }

  /**
   * 访问密钥 - API
   */
  @Service
  public static class AccessKeyAPI extends API {

    /**
     * 创建访问密钥
     *
     * @param env       环境
     * @param accessKey 访问密钥信息
     * @return 访问密钥信息
     */
    public AccessKeyDTO create(Env env, AccessKeyDTO accessKey) {
      return restTemplate.post(env, "apps/{appId}/accesskeys",
          accessKey, AccessKeyDTO.class, accessKey.getAppId());
    }

    /**
     * 通过应用id获取访问密钥列表
     *
     * @param env   环境
     * @param appId 应用id
     * @return 访问密钥列表
     */
    public List<AccessKeyDTO> findByAppId(Env env, String appId) {
      AccessKeyDTO[] accessKeys = restTemplate.get(env, "apps/{appId}/accesskeys",
          AccessKeyDTO[].class, appId);
      return Arrays.asList(accessKeys);
    }

    /**
     * 删除访问密钥
     *
     * @param env      环境
     * @param appId    应用id
     * @param id       访问密钥主键id
     * @param operator 操作者
     */
    public void delete(Env env, String appId, long id, String operator) {
      restTemplate.delete(env, "apps/{appId}/accesskeys/{id}?operator={operator}",
          appId, id, operator);
    }

    /**
     * 开启访问密钥
     *
     * @param env      环境
     * @param appId    应用id
     * @param id       访问密钥id
     * @param operator 操作者
     */
    public void enable(Env env, String appId, long id, String operator) {
      restTemplate.put(env, "apps/{appId}/accesskeys/{id}/enable?operator={operator}",
          null, appId, id, operator);
    }

    /**
     * 关闭访问密钥
     *
     * @param env      环境
     * @param appId    应用id
     * @param id       访问密钥主键id
     * @param operator 操作者
     */
    public void disable(Env env, String appId, long id, String operator) {
      restTemplate.put(env, "apps/{appId}/accesskeys/{id}/disable?operator={operator}",
          null, appId, id, operator);
    }
  }

  /**
   * 发布 - API
   */
  @Service
  public static class ReleaseAPI extends API {

    private static final Joiner JOINER = Joiner.on(",");

    /**
     * 根据发布id查询发布信息
     *
     * @param env       环境
     * @param releaseId 发布id
     * @return 指定id的发布信息
     */
    public ReleaseDTO loadRelease(Env env, long releaseId) {
      return restTemplate.get(env, "releases/{releaseId}", ReleaseDTO.class, releaseId);
    }

    /**
     * 通过id列表找到发布信息列表
     *
     * @param env        环境
     * @param releaseIds 发布id列表
     * @return 发布信息列表
     */
    public List<ReleaseDTO> findReleaseByIds(Env env, Set<Long> releaseIds) {
      if (CollectionUtils.isEmpty(releaseIds)) {
        return Collections.emptyList();
      }

      ReleaseDTO[] releases = restTemplate.get(env, "/releases?releaseIds={releaseIds}",
          ReleaseDTO[].class, JOINER.join(releaseIds));
      return Arrays.asList(releases);

    }

    /**
     * 获取所有发布信息
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param page          页码
     * @param size          页面大小
     * @return 发布信息列表
     */
    public List<ReleaseDTO> findAllReleases(String appId, Env env, String clusterName,
        String namespaceName, int page, int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all?page={page}&size={size}",
          ReleaseDTO[].class, appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    /**
     * 最新的发布信息列表
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param page          页码
     * @param size          页面大小
     * @return 最新的发布信息列表
     */
    public List<ReleaseDTO> findActiveReleases(String appId, Env env, String clusterName,
        String namespaceName, int page, int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active?page={page}&size={size}",
          ReleaseDTO[].class, appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    /**
     * 查询名称空间最近的发布信息
     *
     * @param appId       应用id
     * @param env         环境
     * @param clusterName 集群名称
     * @param namespace   名称空间
     * @return 最近的发布信息
     */
    public ReleaseDTO loadLatestRelease(String appId, Env env, String clusterName,
        String namespace) {
      ReleaseDTO releaseDTO = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest",
          ReleaseDTO.class, appId, clusterName, namespace);
      return releaseDTO;
    }

    /**
     * 创建名称空间发布信息
     *
     * @param appId              应用id
     * @param env                环境
     * @param clusterName        集群名称
     * @param namespace          名称空间名称
     * @param releaseName        发布名称
     * @param releaseComment     发布备注
     * @param operator           操作人
     * @param isEmergencyPublish 是否紧急发布
     * @return 创建的发布信息
     */
    public ReleaseDTO createRelease(String appId, Env env, String clusterName, String namespace,
        String releaseName, String releaseComment, String operator, boolean isEmergencyPublish) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          String.format("%s;charset=UTF-8", MediaType.APPLICATION_FORM_URLENCODED_VALUE)));
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("name", releaseName);
      parameters.add("comment", releaseComment);
      parameters.add("operator", operator);
      parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);
      ReleaseDTO response = restTemplate.post(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", entity,
          ReleaseDTO.class, appId, clusterName, namespace);
      return response;
    }

    /**
     * 发布
     *
     * @param appId              应用id
     * @param env                环境
     * @param clusterName        集群名称
     * @param namespace          名称空间名称
     * @param operator           操作者
     * @param releaseName        发布名称
     * @param releaseComment     发布备注
     * @param isEmergencyPublish 是否紧急发布
     * @param grayDelKeys        灰度发布待删除的规则key
     * @return 发布信息
     */
    public ReleaseDTO createGrayDeletionRelease(String appId, Env env, String clusterName,
        String namespace, String releaseName, String releaseComment, String operator,
        boolean isEmergencyPublish, Set<String> grayDelKeys) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          String.format("%s;charset=UTF-8", MediaType.APPLICATION_FORM_URLENCODED_VALUE)));
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("releaseName", releaseName);
      parameters.add("comment", releaseComment);
      parameters.add("operator", operator);
      parameters.add("isEmergencyPublish", String.valueOf(isEmergencyPublish));
      grayDelKeys.forEach(key -> parameters.add("grayDelKeys", key));
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);
      ReleaseDTO response = restTemplate.post(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/gray-del-releases",
          entity, ReleaseDTO.class, appId, clusterName, namespace);
      return response;
    }

    /**
     * 更新并发布（合并分支属性配置项至master并且发布master）
     *
     * @param appId              应用id
     * @param env                环境
     * @param clusterName        集群id
     * @param namespace          名称空间名称
     * @param releaseName        发布名称
     * @param branchName         分支名称
     * @param deleteBranch       删除分支
     * @param releaseComment     发布备注
     * @param isEmergencyPublish 是否紧急发布
     * @param changeSets         改变的配置集
     * @return 发布信息
     */
    public ReleaseDTO updateAndPublish(String appId, Env env, String clusterName, String namespace,
        String releaseName, String releaseComment, String branchName,
        boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSets changeSets) {

      return restTemplate.post(env, String.format(
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/updateAndPublish?releaseName={releaseName}&releaseComment={releaseComment}&branchName={branchName}&deleteBranch={deleteBranch}&isEmergencyPublish={isEmergencyPublish}"),
          changeSets, ReleaseDTO.class, appId, clusterName, namespace,
          releaseName, releaseComment, branchName, deleteBranch, isEmergencyPublish);

    }

    /**
     * 回滚
     *
     * @param env       环境
     * @param releaseId 开始的发布id
     * @param operator  操作者
     */
    public void rollback(Env env, long releaseId, String operator) {
      restTemplate.put(env, "releases/{releaseId}/rollback?operator={operator}", null,
          releaseId, operator);
    }

    /**
     * 回滚
     *
     * @param releaseId   开始的发布id
     * @param toReleaseId 结束的发布id
     * @param operator    操作者
     */
    public void rollbackTo(Env env, long releaseId, long toReleaseId, String operator) {
      restTemplate.put(env,
          "releases/{releaseId}/rollback?toReleaseId={toReleaseId}&operator={operator}",
          null, releaseId, toReleaseId, operator);
    }
  }

  /**
   * 提交记录 - API
   */
  @Service
  public static class CommitAPI extends API {

    /**
     * 查询提交记录
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param page          页码
     * @param size          页面大小
     * @return 提交记录列表信息
     */
    public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName,
        int page, int size) {

      CommitDTO[] commitDTOs = restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit?page={page}&size={size}",
          CommitDTO[].class, appId, clusterName, namespaceName, page, size);
      return Arrays.asList(commitDTOs);
    }
  }

  /**
   * 名称空间锁 -API
   */
  @Service
  public static class NamespaceLockAPI extends API {

    /**
     * 获取名称空间锁信息
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 名称空间锁信息
     */
    public NamespaceLockDTO getNamespaceLockOwner(String appId, Env env, String clusterName,
        String namespaceName) {
      return restTemplate.get(env,
          "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock",
          NamespaceLockDTO.class, appId, clusterName, namespaceName);
    }
  }

  /**
   * 实例 - API
   */
  @Service
  public static class InstanceAPI extends API {

    private Joiner joiner = Joiner.on(",");
    private ParameterizedTypeReference<PageDTO<InstanceDTO>>
        pageInstanceDtoType = new ParameterizedTypeReference<PageDTO<InstanceDTO>>() {
    };

    /**
     * 获取指定环境的指定发布信息的实例分页信息
     *
     * @param env       环境
     * @param releaseId 发布id
     * @param page      页码
     * @param size      页面大小
     * @return 指定环境的指定发布信息的实例分页信息
     */
    public PageDTO<InstanceDTO> getByRelease(Env env, long releaseId, int page, int size) {
      ResponseEntity<PageDTO<InstanceDTO>> entity = restTemplate
          .get(env, "/instances/by-release?releaseId={releaseId}&page={page}&size={size}",
              pageInstanceDtoType, releaseId, page, size);
      return entity.getBody();

    }

    /**
     * 查询不为指定发布key集合的实例列表信息
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param releaseIds    发布id集
     * @return 实例列表信息
     */
    public List<InstanceDTO> getByReleasesNotIn(String appId, Env env, String clusterName,
        String namespaceName, Set<Long> releaseIds) {

      InstanceDTO[] instanceDTOs = restTemplate.get(env,
          "/instances/by-namespace-and-releases-not-in?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}&releaseIds={releaseIds}",
          InstanceDTO[].class, appId, clusterName, namespaceName, joiner.join(releaseIds));

      return Arrays.asList(instanceDTOs);
    }

    /**
     * 获取指定环境的指定名称空间实例分页信息
     *
     * @param appId         发布id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param instanceAppId 实例应用id
     * @param page          页码
     * @param size          页面大小
     * @return 指定环境的指定名称空间实例分页信息
     */
    public PageDTO<InstanceDTO> getByNamespace(String appId, Env env, String clusterName,
        String namespaceName, String instanceAppId, int page, int size) {
      ResponseEntity<PageDTO<InstanceDTO>> entity = restTemplate.get(env, String.format(
          "/instances/by-namespace?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}&instanceAppId={instanceAppId}&page={page}&size={size}"),
          pageInstanceDtoType, appId, clusterName, namespaceName, instanceAppId, page, size);
      return entity.getBody();
    }

    /**
     * 通过名称空间获取实例数量
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 指定名称空间的实例数量
     */
    public int getInstanceCountByNamespace(String appId, Env env, String clusterName,
        String namespaceName) {
      Integer count = restTemplate.get(env,
          "/instances/by-namespace/count?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}",
          Integer.class, appId, clusterName, namespaceName);
      if (count == null) {
        return 0;
      }
      return count;
    }
  }

  /**
   * 名称空间分支 - API
   */
  @Service
  public static class NamespaceBranchAPI extends API {

    /**
     * 创建分支的子名称空间
     *
     * @param appId         应用名称
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param operator      操作者
     * @return 创建的分支子名称空间信息
     */
    public NamespaceDTO createBranch(String appId, Env env, String clusterName,
        String namespaceName, String operator) {
      return restTemplate.post(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches?operator={operator}",
          null, NamespaceDTO.class, appId, clusterName, namespaceName, operator);
    }

    /**
     * 查询名称空间分支信息
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @return 名称空间信息
     */
    public NamespaceDTO findBranch(String appId, Env env, String clusterName,
        String namespaceName) {
      return restTemplate.get(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches",
          NamespaceDTO.class, appId, clusterName, namespaceName);
    }

    /**
     * 查询名称空间分支（子名称空间）灰度发布规则信息
     *
     * @param appId         应用名称
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param branchName    分支名称
     * @return 灰度发布规则信息
     */
    public GrayReleaseRuleDTO findBranchGrayRules(String appId, Env env, String clusterName,
        String namespaceName, String branchName) {
      return restTemplate.get(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
          GrayReleaseRuleDTO.class, appId, clusterName, namespaceName, branchName);

    }

    /**
     * 更新名称空间分支(子名称空间)灰度发布规则
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param branchName    分支名称
     * @param rules         灰度发布规则信息
     */
    public void updateBranchGrayRules(String appId, Env env, String clusterName,
        String namespaceName, String branchName, GrayReleaseRuleDTO rules) {
      restTemplate.put(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules",
          rules, appId, clusterName, namespaceName, branchName);

    }

    /**
     * 删除分支
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param branchName    分支名称
     * @param operator      操作人
     */
    public void deleteBranch(String appId, Env env, String clusterName,
        String namespaceName, String branchName, String operator) {
      restTemplate.delete(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}?operator={operator}",
          appId, clusterName, namespaceName, branchName, operator);
    }
  }

  /**
   * 发布历史记录 - API
   */
  @Service
  public static class ReleaseHistoryAPI extends API {

    private ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>> type =
        new ParameterizedTypeReference<PageDTO<ReleaseHistoryDTO>>() {
        };

    /**
     * 通过名称空间查找发布历史记录
     *
     * @param appId         应用id
     * @param env           环境
     * @param clusterName   集群名称
     * @param namespaceName 名称空间名称
     * @param page          页码
     * @param size          页面大小
     * @return 发布历史记录分页信息
     */
    public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(String appId, Env env,
        String clusterName, String namespaceName, int page, int size) {
      return restTemplate.get(env,
          "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories?page={page}&size={size}",
          type, appId, clusterName, namespaceName, page, size).getBody();
    }

    /**
     * 根据发布id和操作查询发布历史信息
     *
     * @param env       环境
     * @param releaseId 发布id
     * @param operation 发布操作
     * @param page      页码
     * @param size      页面大小
     * @return 发布历史记录分页信息
     */
    public PageDTO<ReleaseHistoryDTO> findByReleaseIdAndOperation(Env env, long releaseId,
        int operation, int page, int size) {
      return restTemplate.get(env,
          "/releases/histories/by_release_id_and_operation?releaseId={releaseId}&operation={operation}&page={page}&size={size}",
          type, releaseId, operation, page, size).getBody();
    }

    /**
     * 查询指定之前的发布id和指定发布操作的发布历史信息
     *
     * @param env               环境
     * @param previousReleaseId 之前的发布id
     * @param operation         发布操作
     * @param page              页码
     * @param size              页面大小
     * @return 发布历史记录分页信息
     */
    public PageDTO<ReleaseHistoryDTO> findByPreviousReleaseIdAndOperation(Env env,
        long previousReleaseId, int operation, int page, int size) {
      return restTemplate.get(env,
          "/releases/histories/by_previous_release_id_and_operation?previousReleaseId={releaseId}&operation={operation}&page={page}&size={size}",
          type, previousReleaseId, operation, page, size).getBody();
    }

  }

}
