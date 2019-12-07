package com.ctrip.framework.apollo.openapi.util;

import com.ctrip.framework.apollo.common.dto.*;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.openapi.dto.*;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class OpenApiBeanUtils {

  private static Gson gson = new Gson();
  private static Type type = new TypeToken<Map<String, String>>() {}.getType();

  public static OpenItemDTO transformFromItemDTO(ItemDTO item) {
    Preconditions.checkArgument(item != null);
    return BeanUtils.transform(OpenItemDTO.class, item);
  }

  public static ItemDTO transformToItemDTO(OpenItemDTO openItemDTO) {
    Preconditions.checkArgument(openItemDTO != null);
    return BeanUtils.transform(ItemDTO.class, openItemDTO);
  }

  public static OpenAppNamespaceDTO transformToOpenAppNamespaceDTO(AppNamespace appNamespace) {
    Preconditions.checkArgument(appNamespace != null);
    return BeanUtils.transform(OpenAppNamespaceDTO.class, appNamespace);
  }

  public static AppNamespace transformToAppNamespace(OpenAppNamespaceDTO openAppNamespaceDTO) {
    Preconditions.checkArgument(openAppNamespaceDTO != null);
    return BeanUtils.transform(AppNamespace.class, openAppNamespaceDTO);
  }

  public static OpenReleaseDTO transformFromReleaseDTO(ReleaseDTO release) {
    Preconditions.checkArgument(release != null);

    OpenReleaseDTO openReleaseDTO = BeanUtils.transform(OpenReleaseDTO.class, release);

    Map<String, String> configs = gson.fromJson(release.getConfigurations(), type);

    openReleaseDTO.setConfigurations(configs);
    return openReleaseDTO;
  }

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

  public static OpenNamespaceLockDTO transformFromNamespaceLockDTO(String namespaceName,
      NamespaceLockDTO namespaceLock) {
    OpenNamespaceLockDTO lock = new OpenNamespaceLockDTO();

    lock.setNamespaceName(namespaceName);

    if (namespaceLock == null) {
      lock.setLocked(false);
    } else {
      lock.setLocked(true);
      lock.setLockedBy(namespaceLock.getDataChangeCreatedBy());
    }

    return lock;
  }

  public static OpenGrayReleaseRuleDTO transformFromGrayReleaseRuleDTO(
      GrayReleaseRuleDTO grayReleaseRuleDTO) {
    Preconditions.checkArgument(grayReleaseRuleDTO != null);

    return BeanUtils.transform(OpenGrayReleaseRuleDTO.class, grayReleaseRuleDTO);
  }

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

  public static List<OpenAppDTO> transformFromApps(final List<App> apps) {
    if (CollectionUtils.isEmpty(apps)) {
      return Collections.emptyList();
    }
    return apps.stream().map(OpenApiBeanUtils::transformFromApp).collect(Collectors.toList());
  }

  public static OpenAppDTO transformFromApp(final App app) {
    Preconditions.checkArgument(app != null);

    return BeanUtils.transform(OpenAppDTO.class, app);
  }

  public static OpenClusterDTO transformFromClusterDTO(ClusterDTO Cluster) {
    Preconditions.checkArgument(Cluster != null);
    return BeanUtils.transform(OpenClusterDTO.class, Cluster);
  }

  public static ClusterDTO transformToClusterDTO(OpenClusterDTO openClusterDTO) {
    Preconditions.checkArgument(openClusterDTO != null);
    return BeanUtils.transform(ClusterDTO.class, openClusterDTO);
  }
}
