package com.ctrip.framework.apollo.biz.service;


import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 属性配置项 Service层
 */
@Service
public class ItemService {

  private final ItemRepository itemRepository;
  private final NamespaceService namespaceService;
  private final AuditService auditService;
  private final BizConfig bizConfig;

  public ItemService(
      final ItemRepository itemRepository,
      final @Lazy NamespaceService namespaceService,
      final AuditService auditService,
      final BizConfig bizConfig) {
    this.itemRepository = itemRepository;
    this.namespaceService = namespaceService;
    this.auditService = auditService;
    this.bizConfig = bizConfig;
  }

  /**
   * 通过配置项id删除配置项
   *
   * @param id       配置项id
   * @param operator 操作者
   * @return 删除的配置项
   */
  @Transactional(rollbackFor = Exception.class)
  public Item delete(long id, String operator) {
    // 逻辑删除
    Item item = itemRepository.findById(id).orElse(null);
    if (item == null) {
      throw new IllegalArgumentException("item not exist. ID:" + id);
    }
    item.setDeleted(true);
    item.setDataChangeLastModifiedBy(operator);
    Item deletedItem = itemRepository.save(item);

    // 记录日志审计信息
    auditService.audit(Item.class.getSimpleName(), id, Audit.OP.DELETE, operator);
    return deletedItem;
  }

  /**
   * 批量删除指定名称空间id的属性配置项
   *
   * @param namespaceId 名称空间id
   * @param operator    操作人
   * @return 影响的行数
   */
  @Transactional(rollbackFor = Exception.class)
  public int batchDelete(long namespaceId, String operator) {
    return itemRepository.deleteByNamespaceId(namespaceId, operator);

  }

  /**
   * 查询指定的配置项
   *
   * @param appId         应用id
   * @param clusterName   集群id
   * @param namespaceName 名称空间名称
   * @param key           配置项的key
   * @return 指定的配置项信息
   */
  public Item findOne(String appId, String clusterName, String namespaceName, String key) {
    // 指定的名称空间信息
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    return itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
  }

  /**
   * 查询指定名称空间最新的配置项信息
   *
   * @param appId         应用id
   * @param clusterName   集群id
   * @param namespaceName 名称空间名称
   * @return 查询指定的配置项
   */
  public Item findLastOne(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    return findLastOne(namespace.getId());
  }

  /**
   * 查询指定名称空间最新的配置项信息
   *
   * @param namespaceId 名称空间Id
   * @return 配置项信息
   */
  public Item findLastOne(long namespaceId) {
    return itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespaceId);
  }

  /**
   * 通过配置项id查询配置项信息
   *
   * @param itemId 配置项id
   * @return 配置项信息
   */
  public Item findOne(long itemId) {
    return itemRepository.findById(itemId).orElse(null);
  }

  /**
   * 查询指定名称空间的所有配置项列表信息（没有排序）
   *
   * @param namespaceId 名称空间Id
   * @return 指定名称空间的所有配置项列表信息（没有排序）
   */
  public List<Item> findItemsWithoutOrdered(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceId(namespaceId);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }

  /**
   * 查询指定名称空间的所有配置项列表信息（没有排序）
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的所有配置项列表信息（没有排序）
   */
  public List<Item> findItemsWithoutOrdered(String appId, String clusterName,
      String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace != null) {
      return findItemsWithoutOrdered(namespace.getId());
    }
    return Collections.emptyList();
  }

  /**
   * 获取指定名称空间的属性的配置项列表（以行号升序）
   *
   * @param namespaceId 名称空间id
   * @return 属性的配置项列表
   */
  public List<Item> findItemsWithOrdered(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespaceId);
    return items == null ? Collections.emptyList() : items;
  }

  /**
   * 查询指定名称空间的属性的配置项列表信息（以行号升序）
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 指定名称空间的属性的配置项列表信息（以行号升序）
   */
  public List<Item> findItemsWithOrdered(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace != null) {
      return findItemsWithOrdered(namespace.getId());
    }
    return Collections.emptyList();
  }

  /**
   * 查询最后修改时间大于指定日期的配置项列表信息
   *
   * @param namespaceId 名称空间id
   * @param date        指定日期
   * @return 配置项列表信息
   */
  public List<Item> findItemsModifiedAfterDate(long namespaceId, Date date) {
    return itemRepository
        .findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(namespaceId, date);
  }

  /**
   * 保存配置项信息
   *
   * @param entity 待保存的配置项信息
   * @return 保存后的配置项信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Item save(Item entity) {
    // 检查key和Value的长度
    checkItemKeyLength(entity.getKey());
    checkItemValueLength(entity.getNamespaceId(), entity.getValue());

    //protection
    entity.setId(0);

    // 设置行号
    if (entity.getLineNum() == 0) {
      Item lastItem = findLastOne(entity.getNamespaceId());
      int lineNum = lastItem == null ? 1 : lastItem.getLineNum() + 1;
      entity.setLineNum(lineNum);
    }

    // 保存
    Item item = itemRepository.save(entity);
    // 记录日志审计信息
    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.INSERT,
        item.getDataChangeCreatedBy());
    return item;
  }

  /**
   * 更新配置项信息
   *
   * @param item 待更新的配置项信息
   * @return 更新后的配置项信息
   */
  @Transactional(rollbackFor = Exception.class)
  public Item update(Item item) {
    // 检查配置项的Value的长度
    checkItemValueLength(item.getNamespaceId(), item.getValue());
    Item managedItem = itemRepository.findById(item.getId()).orElse(null);

    // 以新换旧
    BeanUtils.copyEntityProperties(item, managedItem);
    managedItem = itemRepository.save(managedItem);
    // 记录日志审计信息
    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
        managedItem.getDataChangeLastModifiedBy());

    return managedItem;
  }

  /**
   * 检查配置项的Value的长度
   *
   * @param namespaceId 名称空间id
   * @param value       配置项Value
   * @return true, 符合，否则，抛出异常
   */
  private boolean checkItemValueLength(long namespaceId, String value) {
    int limit = getItemValueLengthLimit(namespaceId);
    if (!StringUtils.isEmpty(value) && value.length() > limit) {
      throw new BadRequestException("value too long. length limit:" + limit);
    }
    return true;
  }

  /**
   * 检查配置项Key的长度
   *
   * @param key 配置项Key
   * @return true, 符合，否则，抛出异常
   */
  private boolean checkItemKeyLength(String key) {
    if (!StringUtils.isEmpty(key) && key.length() > bizConfig.itemKeyLengthLimit()) {
      throw new BadRequestException("key too long. length limit:" + bizConfig.itemKeyLengthLimit());
    }
    return true;
  }

  /**
   * 获取配置项Value长度的限制值
   *
   * @param namespaceId 名称空间id
   * @return 配置项Value长度的限制值
   */
  private int getItemValueLengthLimit(long namespaceId) {
    // 设置的名称空间配置项value最大长度限制
    Map<Long, Integer> namespaceValueLengthOverride = bizConfig.namespaceValueLengthLimitOverride();
    if (namespaceValueLengthOverride != null && namespaceValueLengthOverride
        .containsKey(namespaceId)) {
      return namespaceValueLengthOverride.get(namespaceId);
    }
    // 配置项Value默认限制值
    return bizConfig.itemValueLengthLimit();
  }

}
