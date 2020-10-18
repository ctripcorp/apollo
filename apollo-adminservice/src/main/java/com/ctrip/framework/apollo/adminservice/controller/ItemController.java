package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.adminservice.aop.PreAcquireNamespaceLock;
import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.CommitService;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.utils.ConfigChangeContentBuilder;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 属性配置项 Controller层
 */
@RestController
public class ItemController {

  private final ItemService itemService;
  private final NamespaceService namespaceService;
  private final CommitService commitService;
  private final ReleaseService releaseService;


  public ItemController(final ItemService itemService, final NamespaceService namespaceService,
      final CommitService commitService, final ReleaseService releaseService) {
    this.itemService = itemService;
    this.namespaceService = namespaceService;
    this.commitService = commitService;
    this.releaseService = releaseService;
  }

  /**
   * 添加配置项
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param dto           配置项信息
   * @return 创建后的配置项信息
   */
  @PreAcquireNamespaceLock
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public ItemDTO create(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @RequestBody ItemDTO dto) {
    Item entity = BeanUtils.transform(Item.class, dto);

    ConfigChangeContentBuilder builder = new ConfigChangeContentBuilder();
    // 验证是否存在
    Item managedEntity = itemService.findOne(appId, clusterName, namespaceName, entity.getKey());
    if (managedEntity != null) {
      throw new BadRequestException("item already exists");
    }
    // 添加
    entity = itemService.save(entity);
    builder.createItem(entity);
    dto = BeanUtils.transform(ItemDTO.class, entity);

    // 添加提交记录
    Commit commit = new Commit();
    commit.setAppId(appId);
    commit.setClusterName(clusterName);
    commit.setNamespaceName(namespaceName);
    commit.setChangeSets(builder.build());
    commit.setDataChangeCreatedBy(dto.getDataChangeLastModifiedBy());
    commit.setDataChangeLastModifiedBy(dto.getDataChangeLastModifiedBy());
    commitService.save(commit);

    return dto;
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemId        配置项id
   * @param itemDTO       配置项信息
   * @return 更新后的配置项
   */
  @PreAcquireNamespaceLock
  @PutMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}")
  public ItemDTO update(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName,
      @PathVariable("itemId") long itemId,
      @RequestBody ItemDTO itemDTO) {
    Item managedEntity = itemService.findOne(itemId);
    if (managedEntity == null) {
      throw new NotFoundException("item not found for itemId " + itemId);
    }


    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    // In case someone constructs an attack scenario
    if (namespace == null || namespace.getId() != managedEntity.getNamespaceId()) {
      throw new BadRequestException("Invalid request, item and namespace do not match!");
    }

    // 以新换旧
    Item entity = BeanUtils.transform(Item.class, itemDTO);
    Item beforeUpdateItem = BeanUtils.transform(Item.class, managedEntity);

    ConfigChangeContentBuilder builder = new ConfigChangeContentBuilder();

    // 只有value,comment,lastModifiedBy可以被修改
    managedEntity.setValue(entity.getValue());
    managedEntity.setComment(entity.getComment());
    managedEntity.setDataChangeLastModifiedBy(entity.getDataChangeLastModifiedBy());

    // 更新
    entity = itemService.update(managedEntity);
    builder.updateItem(beforeUpdateItem, entity);
    itemDTO = BeanUtils.transform(ItemDTO.class, entity);

    // 添加提交记录
    if (builder.hasContent()) {
      Commit commit = new Commit();
      commit.setAppId(appId);
      commit.setClusterName(clusterName);
      commit.setNamespaceName(namespaceName);
      commit.setChangeSets(builder.build());
      commit.setDataChangeCreatedBy(itemDTO.getDataChangeLastModifiedBy());
      commit.setDataChangeLastModifiedBy(itemDTO.getDataChangeLastModifiedBy());
      commitService.save(commit);
    }

    return itemDTO;
  }

  /**
   * 删除配置项
   *
   * @param itemId   配置项id
   * @param operator 操作人
   */
  @PreAcquireNamespaceLock
  @DeleteMapping("/items/{itemId}")
  public void delete(@PathVariable("itemId") long itemId, @RequestParam String operator) {
    Item entity = itemService.findOne(itemId);
    if (entity == null) {
      throw new NotFoundException("item not found for itemId " + itemId);
    }
    //删除
    itemService.delete(entity.getId(), operator);

    Namespace namespace = namespaceService.findOne(entity.getNamespaceId());

    // 添加提交记录
    Commit commit = new Commit();
    commit.setAppId(namespace.getAppId());
    commit.setClusterName(namespace.getClusterName());
    commit.setNamespaceName(namespace.getNamespaceName());
    commit.setChangeSets(new ConfigChangeContentBuilder().deleteItem(entity).build());
    commit.setDataChangeCreatedBy(operator);
    commit.setDataChangeLastModifiedBy(operator);
    commitService.save(commit);
  }

  /**
   * 查询指定名称空间的属性的配置项列表信息（以行号升序）
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的属性的配置项列表信息（以行号升序）
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    return BeanUtils.batchTransform(ItemDTO.class,
        itemService.findItemsWithOrdered(appId, clusterName, namespaceName));
  }

  /**
   * 查询已经被删除的配置项信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 已经被删除的配置项信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/deleted")
  public List<ItemDTO> findDeletedItems(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    //名称空间上一次的发布信息
    Release latestActiveRelease = releaseService.findLatestActiveRelease(appId, clusterName,
        namespaceName);

    // 获取提交记录
    List<Commit> commits;
    if (Objects.nonNull(latestActiveRelease)) {
      commits = commitService.find(appId, clusterName, namespaceName,
          latestActiveRelease.getDataChangeCreatedTime(), null);
    } else {
      commits = commitService.find(appId, clusterName, namespaceName, null);
    }

    // 删除的配置项信息
    if (Objects.nonNull(commits)) {
      List<Item> deletedItems = commits.stream().map(item ->
          ConfigChangeContentBuilder.convertJsonString(item.getChangeSets()).getDeleteItems())
          .flatMap(Collection::stream).collect(Collectors.toList());
      return BeanUtils.batchTransform(ItemDTO.class, deletedItems);
    }
    return Collections.emptyList();
  }

  /**
   * 通过配置项id找到配置项信息
   *
   * @param itemId 配置项id
   * @return 指定配置项id的配置项信息
   */
  @GetMapping("/items/{itemId}")
  public ItemDTO get(@PathVariable("itemId") long itemId) {
    Item item = itemService.findOne(itemId);
    if (item == null) {
      throw new NotFoundException("item not found for itemId " + itemId);
    }
    return BeanUtils.transform(ItemDTO.class, item);
  }

  /**
   * 找到定Key的配置项信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param key           配置项Key
   * @return 指定Key的配置项信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key:.+}")
  public ItemDTO get(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @PathVariable("key") String key) {
    Item item = itemService.findOne(appId, clusterName, namespaceName, key);
    if (item == null) {
      throw new NotFoundException(
          String.format("item not found for %s %s %s %s", appId, clusterName, namespaceName, key));
    }
    return BeanUtils.transform(ItemDTO.class, item);
  }


}
