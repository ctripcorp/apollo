package com.ctrip.framework.apollo.biz.service;


import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private AuditService auditService;

  @Autowired
  private BizConfig bizConfig;


  @Transactional
  public Item delete(long id, String operator) {
    Item item = itemRepository.findOne(id);
    if (item == null) {
      throw new IllegalArgumentException("item not exist. ID:" + id);
    }

    item.setDeleted(true);
    item.setDataChangeLastModifiedBy(operator);
    Item deletedItem = itemRepository.save(item);

    auditService.audit(Item.class.getSimpleName(), id, Audit.OP.DELETE, operator);
    return deletedItem;
  }

  @Transactional
  public int batchDelete(long namespaceId, String operator) {
    return itemRepository.deleteByNamespaceId(namespaceId, operator);

  }

  public Item findOne(String appId, String clusterName, String namespaceName, String key) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
    return item;
  }

  public Item findLastOne(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    return findLastOne(namespace.getId());
  }

  public Item findLastOne(long namespaceId) {
    PageRequest pageRequest = new PageRequest(0, 1);
    List<Item> result = itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespaceId, pageRequest);

    if (result.size() > 0) {
      return result.get(0);
    } else {
      return null;
    }
  }

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

  public List<Item> findItemsWithoutOrdered(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceId(namespaceId);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }

  public List<Item> findItemsWithoutOrdered(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace != null) {
      return findItemsWithoutOrdered(namespace.getId());
    } else {
      return Collections.emptyList();
    }
  }

  public List<Item> findItemsWithOrdered(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespaceId);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }

  public List<Item> findItemsWithOrdered(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace != null) {
      return findItemsWithOrdered(namespace.getId());
    } else {
      return Collections.emptyList();
    }
  }

  public List<Item> findItemsModifiedAfterDate(long namespaceId, Date date) {
    return itemRepository.findByNamespaceIdAndDataChangeLastModifiedTimeGreaterThan(namespaceId, date);
  }

  public Item findFirst1ByKeyOrderByDataChangeLastModifiedTime(long namespaceId, String key) {
    PageRequest page = new PageRequest(0, 1);
    List<Item> itemList = itemRepository.findFirst1ByKeyOrderByDataChangeLastModifiedTime(namespaceId, key, page);

    if (itemList.size() > 0) {
      return itemList.get(0);
    } else {
      return null;
    }
  }

  @Transactional
  public Item save(Item entity) {
    checkItemKeyLength(entity.getKey());
    checkItemValueLength(entity.getNamespaceId(), entity.getValue());

    entity.setId(0);//protection

    if (entity.getLineNum() == 0) {
      Item lastItem = findLastOne(entity.getNamespaceId());
      int lineNum = lastItem == null ? 1 : lastItem.getLineNum() + 1;
      entity.setLineNum(lineNum);
    }

    Item item = itemRepository.save(entity);

    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.INSERT,
                       item.getDataChangeCreatedBy());

    return item;
  }

  @Transactional
  public Item naiveSave(Item entity) {
    Item item = itemRepository.save(entity);

    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.UPDATE,
            item.getDataChangeCreatedBy());

    return item;
  }

  @Transactional
  public Item update(Item item) {
    checkItemValueLength(item.getNamespaceId(), item.getValue());
    Item managedItem = itemRepository.findOne(item.getId());
    BeanUtils.copyEntityProperties(item, managedItem);
    managedItem = itemRepository.save(managedItem);

    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
                       managedItem.getDataChangeLastModifiedBy());

    return managedItem;
  }

  private boolean checkItemValueLength(long namespaceId, String value) {
    int limit = getItemValueLengthLimit(namespaceId);
    if (!StringUtils.isEmpty(value) && value.length() > limit) {
      throw new BadRequestException("value too long. length limit:" + limit);
    }
    return true;
  }

  private boolean checkItemKeyLength(String key) {
    if (!StringUtils.isEmpty(key) && key.length() > bizConfig.itemKeyLengthLimit()) {
      throw new BadRequestException("key too long. length limit:" + bizConfig.itemKeyLengthLimit());
    }
    return true;
  }

  private int getItemValueLengthLimit(long namespaceId) {
    Map<Long, Integer> namespaceValueLengthOverride = bizConfig.namespaceValueLengthLimitOverride();
    if (namespaceValueLengthOverride != null && namespaceValueLengthOverride.containsKey(namespaceId)) {
      return namespaceValueLengthOverride.get(namespaceId);
    }
    return bizConfig.itemValueLengthLimit();
  }

}
