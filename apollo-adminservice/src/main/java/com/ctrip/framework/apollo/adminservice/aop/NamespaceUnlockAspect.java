package com.ctrip.framework.apollo.adminservice.aop;


import com.ctrip.framework.apollo.adminservice.controller.ItemController;
import com.ctrip.framework.apollo.adminservice.controller.ItemSetController;
import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 名称空间解锁的切面
 * <p>
 * 如果是重做操作，则解锁命名空间
 * <p>
 *
 * <pre>
 * --------------------------------------------
 * 示例: 如果名称空间有一个配置项 K1 = v1
 * --------------------------------------------
 * 第二次操作: change k1 = v2 (lock namespace)
 * 第二次操作: change k1 = v1 (unlock namespace)
 * </pre>
 */
@Aspect
@Component
public class NamespaceUnlockAspect {

  private static final Gson GSON = new Gson();

  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final ItemService itemService;
  private final ReleaseService releaseService;
  private final BizConfig bizConfig;

  public NamespaceUnlockAspect(
      final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService,
      final ItemService itemService,
      final ReleaseService releaseService,
      final BizConfig bizConfig) {
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.itemService = itemService;
    this.releaseService = releaseService;
    this.bizConfig = bizConfig;
  }


  /**
   * 创建配置项之后解锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param item          配置项
   * @see ItemController#create(String, String, String, ItemDTO)
   */
  @After(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)", argNames = "appId,clusterName,namespaceName,item")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
      ItemDTO item) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  /**
   * 更新配置项之后解锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemId        配置项id
   * @param item          配置项
   * @see ItemController#update(String, String, String, long, ItemDTO)
   */
  @After(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)", argNames = "appId,clusterName,namespaceName,itemId,item")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
      ItemDTO item) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  /**
   * 更新配置项变化之后解锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param changeSet     配置项变化的集合列表数据
   * @see ItemSetController#create(String, String, String, ItemChangeSets)
   */
  @After(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)", argNames = "appId,clusterName,namespaceName,changeSet")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
      ItemChangeSets changeSet) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  /**
   * 删除配置项之后解锁
   *
   * @param itemId   配置项id
   * @param operator 操作人
   * @see ItemController#delete(long, String)
   */
  @After(value = "@annotation(PreAcquireNamespaceLock) && args(itemId, operator, ..)", argNames = "itemId,operator")
  public void requireLockAdvice(long itemId, String operator) {
    Item item = itemService.findOne(itemId);
    if (item == null) {
      throw new BadRequestException("item not exist.");
    }
    tryUnlock(namespaceService.findOne(item.getNamespaceId()));
  }

  /**
   * 解锁
   *
   * @param namespace 名称空间信息
   */
  private void tryUnlock(Namespace namespace) {
    // 关闭名称空间锁时直接退出
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    if (!isModified(namespace)) {
      namespaceLockService.unlock(namespace.getId());
    }

  }

  /**
   * 名称空间信息是否被修改
   *
   * @param namespace 名称空间信息
   * @return true, 被修改了，否则，false
   */
  boolean isModified(Namespace namespace) {
    // 名称空间上一次的发布信息
    Release release = releaseService.findLatestActiveRelease(namespace);
    // 名称空间的配置项列表
    List<Item> items = itemService.findItemsWithoutOrdered(namespace.getId());

    if (release == null) {
      return hasNormalItems(items);
    }

    // 将上一次的发布信息发布的配置项转换为配置Map
    Map<String, String> releasedConfiguration = GSON.fromJson(release.getConfigurations(),
        GsonType.CONFIG);
    Map<String, String> configurationFromItems = generateConfigurationFromItems(namespace, items);

    MapDifference<String, String> difference = Maps.difference(releasedConfiguration,
        configurationFromItems);
    return !difference.areEqual();

  }

  /**
   * 是否存在配置项
   *
   * @param items 配置项列表
   * @return true, 存在配置项，否则，false
   */
  private boolean hasNormalItems(List<Item> items) {
    for (Item item : items) {
      if (StringUtils.isNotBlank(item.getKey())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 通过配置项生成配置Map
   *
   * @param namespace      名称空间
   * @param namespaceItems 名称空间配置项
   * @return 生成后的配置项Map
   */
  private Map<String, String> generateConfigurationFromItems(Namespace namespace,
      List<Item> namespaceItems) {

    Map<String, String> configurationFromItems = Maps.newHashMap();

    // 父名称空间信息
    Namespace parentNamespace = namespaceService.findParentNamespace(namespace);
    // 父名称空间配置项生成配置Map
    if (parentNamespace == null) {
      generateMapFromItems(namespaceItems, configurationFromItems);
    } else {
      // 名称空间上一次的发布信息
      Release parentRelease = releaseService.findLatestActiveRelease(parentNamespace);
      // 将上一次的发布信息发布的配置项转换为配置Map
      if (parentRelease != null) {
        configurationFromItems = GSON.fromJson(parentRelease.getConfigurations(), GsonType.CONFIG);
      }
      generateMapFromItems(namespaceItems, configurationFromItems);
    }

    return configurationFromItems;
  }

  /**
   * 将配置项转为Map<key,value>
   *
   * @param items                  配置项列表
   * @param configurationFromItems 配置项Map
   * @return 转换后的配置项Map
   */
  private Map<String, String> generateMapFromItems(List<Item> items,
      Map<String, String> configurationFromItems) {
    for (Item item : items) {
      String key = item.getKey();
      if (StringUtils.isBlank(key)) {
        continue;
      }
      configurationFromItems.put(key, item.getValue());
    }

    return configurationFromItems;
  }

}
