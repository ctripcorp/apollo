package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenGrayReleaseRuleDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenGrayReleaseRuleItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * 开放API工具类
 */
public class OpenApiBeanUtils {

  private static final Gson GSON = new Gson();
  private static Type type = new TypeToken<Map<String, String>>() {
  }.getType();

  /**
   * 将配置项Dto转为开放Api的配置项Dto
   *
   * @param item 配置项Dto
   * @return 开放Api的配置项Dto信息
   */
  public static OpenItemDTO transformFromItemDTO(ItemDTO item) {
    Preconditions.checkArgument(item != null);
    return BeanUtils.transform(OpenItemDTO.class, item);
  }

  /**
   * 将开放Api的配置项Dto转为配置项Dto
   *
   * @param openItemDTO 开放Api的配置项Dto
   * @return 配置项Dto信息
   */
  public static ItemDTO transformToItemDTO(OpenItemDTO openItemDTO) {
    Preconditions.checkArgument(openItemDTO != null);
    return BeanUtils.transform(ItemDTO.class, openItemDTO);
  }

  /**
   * 将应用名称空间转为开放Api的应用名称空间Dto
   *
   * @param appNamespace 应用名称空间
   * @return 应用名称空间Dto信息
   */
  public static OpenAppNamespaceDTO transformToOpenAppNamespaceDTO(AppNamespace appNamespace) {
    Preconditions.checkArgument(appNamespace != null);
    return BeanUtils.transform(OpenAppNamespaceDTO.class, appNamespace);
  }

  /**
   * 将开放Api的应用名称空间Dto转为应用名称空间
   *
   * @param openAppNamespaceDTO 开放Api的应用名称空间Dto
   * @return 应用名称空间信息
   */
  public static AppNamespace transformToAppNamespace(OpenAppNamespaceDTO openAppNamespaceDTO) {
    Preconditions.checkArgument(openAppNamespaceDTO != null);
    return BeanUtils.transform(AppNamespace.class, openAppNamespaceDTO);
  }

  /**
   * 将发布信息Dto转为开放Api的发布信息Dto
   *
   * @param release 发布信息Dto
   * @return 开放Api的发布信息Dto
   */
  public static OpenReleaseDTO transformFromReleaseDTO(ReleaseDTO release) {
    Preconditions.checkArgument(release != null);

    OpenReleaseDTO openReleaseDTO = BeanUtils.transform(OpenReleaseDTO.class, release);

    Map<String, String> configs = GSON.fromJson(release.getConfigurations(), type);

    openReleaseDTO.setConfigurations(configs);
    return openReleaseDTO;
  }

  /**
   * 将名称空间业务对象信息转为开放Api的名称空间Dto信息
   *
   * @param namespaceBO 开放Api的灰度发布规则Dto信息
   * @return 开放Api的名称空间Dto信息
   */
  public static OpenNamespaceDTO transformFromNamespaceBO(NamespaceBO namespaceBO) {
    Preconditions.checkArgument(namespaceBO != null);

    OpenNamespaceDTO openNamespaceDTO =
        BeanUtils.transform(OpenNamespaceDTO.class, namespaceBO.getBaseInfo());

    // app namespace info
    openNamespaceDTO.setFormat(namespaceBO.getFormat());
    openNamespaceDTO.setComment(namespaceBO.getComment());
    openNamespaceDTO.setPublic(namespaceBO.isPublic());

    // items
    List<OpenItemDTO> items = new LinkedList<>();
    List<ItemBO> itemBOs = namespaceBO.getItems();
    if (!CollectionUtils.isEmpty(itemBOs)) {
      items.addAll(itemBOs.stream().map(itemBO -> transformFromItemDTO(itemBO.getItem()))
          .collect(Collectors.toList()));
    }
    openNamespaceDTO.setItems(items);
    return openNamespaceDTO;

  }

  /**
   * 将名称空间业务列表信息转为开放Api的名称空间Dto信息
   *
   * @param namespaceBOs 名称空间业务列表信息
   * @return 开放Api的名称空间Dto信息
   */
  public static List<OpenNamespaceDTO> batchTransformFromNamespaceBOs(
      List<NamespaceBO> namespaceBOs) {
    if (CollectionUtils.isEmpty(namespaceBOs)) {
      return Collections.emptyList();
    }

    List<OpenNamespaceDTO> openNamespaceDTOs =
        namespaceBOs.stream().map(OpenApiBeanUtils::transformFromNamespaceBO)
            .collect(Collectors.toCollection(LinkedList::new));

    return openNamespaceDTOs;
  }

  /**
   * 将开放Api的名称空间锁Dto信息转为名称空间锁信息
   *
   * @param namespaceName 名称空间名称
   * @param namespaceLock 名称空间锁信息
   * @return 灰度发布规则Dto信息
   */
  public static OpenNamespaceLockDTO transformFromNamespaceLockDTO(String namespaceName,
      NamespaceLockDTO namespaceLock) {
    OpenNamespaceLockDTO lock = new OpenNamespaceLockDTO();

    lock.setNamespaceName(namespaceName);

    if (namespaceLock == null) {
      lock.setIsLocked(false);
    } else {
      lock.setIsLocked(true);
      lock.setLockedBy(namespaceLock.getDataChangeCreatedBy());
    }

    return lock;
  }

  /**
   * 将灰度发布规则Dto信息转为开放Api的灰度发布规则Dto信息
   *
   * @param grayReleaseRuleDTO 灰度发布规则Dto信息
   * @return 开放Api的灰度发布规则Dto信息信息
   */
  public static OpenGrayReleaseRuleDTO transformFromGrayReleaseRuleDTO(
      GrayReleaseRuleDTO grayReleaseRuleDTO) {
    Preconditions.checkArgument(grayReleaseRuleDTO != null);

    return BeanUtils.transform(OpenGrayReleaseRuleDTO.class, grayReleaseRuleDTO);
  }

  /**
   * 将开放Api的灰度发布规则Dto信息转为灰度发布规则Dto信息
   *
   * @param openGrayReleaseRuleDTO 开放Api的灰度发布规则Dto信息
   * @return 灰度发布规则Dto信息
   */
  public static GrayReleaseRuleDTO transformToGrayReleaseRuleDTO(
      OpenGrayReleaseRuleDTO openGrayReleaseRuleDTO) {
    Preconditions.checkArgument(openGrayReleaseRuleDTO != null);

    String appId = openGrayReleaseRuleDTO.getAppId();
    String branchName = openGrayReleaseRuleDTO.getBranchName();
    String clusterName = openGrayReleaseRuleDTO.getClusterName();
    String namespaceName = openGrayReleaseRuleDTO.getNamespaceName();

    GrayReleaseRuleDTO grayReleaseRuleDTO =
        new GrayReleaseRuleDTO(appId, clusterName, namespaceName, branchName);

    Set<OpenGrayReleaseRuleItemDTO> openGrayReleaseRuleItemDTOSet =
        openGrayReleaseRuleDTO.getRuleItems();
    openGrayReleaseRuleItemDTOSet.forEach(openGrayReleaseRuleItemDTO -> {
      String clientAppId = openGrayReleaseRuleItemDTO.getClientAppId();
      Set<String> clientIpList = openGrayReleaseRuleItemDTO.getClientIpList();
      GrayReleaseRuleItemDTO ruleItem = new GrayReleaseRuleItemDTO(clientAppId, clientIpList);
      grayReleaseRuleDTO.addRuleItem(ruleItem);
    });

    return grayReleaseRuleDTO;
  }

  /**
   * 将应用列表转为开放Api的应用Dto列表信息
   *
   * @param apps 应用列表
   * @return 开放Api的应用Dto列表信息
   */
  public static List<OpenAppDTO> transformFromApps(final List<App> apps) {
    if (CollectionUtils.isEmpty(apps)) {
      return Collections.emptyList();
    }
    return apps.stream().map(OpenApiBeanUtils::transformFromApp).collect(Collectors.toList());
  }

  /**
   * 将应用信息转为开放Api的应用Dto信息
   *
   * @param app 应用信息
   * @return 开放Api的应用Dto信息
   */
  public static OpenAppDTO transformFromApp(final App app) {
    Preconditions.checkArgument(app != null);

    return BeanUtils.transform(OpenAppDTO.class, app);
  }

  /**
   * 将集群Dto信息转为开放Api的集群Dto信息
   *
   * @param Cluster 集群Dto信息
   * @return 开放Api的集群Dto信息
   */
  public static OpenClusterDTO transformFromClusterDTO(ClusterDTO Cluster) {
    Preconditions.checkArgument(Cluster != null);
    return BeanUtils.transform(OpenClusterDTO.class, Cluster);
  }

  /**
   * 将开放Api的集群Dto转为集群Dto信息
   *
   * @param openClusterDTO 开放Api的集群Dto
   * @return 集群Dto信息
   */
  public static ClusterDTO transformToClusterDTO(OpenClusterDTO openClusterDTO) {
    Preconditions.checkArgument(openClusterDTO != null);
    return BeanUtils.transform(ClusterDTO.class, openClusterDTO);
  }
}
