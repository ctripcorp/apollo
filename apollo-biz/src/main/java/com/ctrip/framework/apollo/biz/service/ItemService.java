package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.exception.NotFoundException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private AuditService auditService;

  @Autowired
  private ServerConfigService serverConfigService;

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

  public Item findOne(String appId, String clusterName, String namespaceName, String key) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
        clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
    return item;
  }

  public Item findLastOne(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
                                                                                        clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespace.getId());
    return item;
  }

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

  public List<Item> findItems(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespaceId);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }
  
  public List<Item> findItems(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
    if (namespace != null) {
      return findItems(namespace.getId());
    } else {
      return Collections.emptyList();
    }
  }
  
  @Transactional
  public Item save(Item entity) {
    checkItemKeyLength(entity.getKey());
    checkItemValueLength(entity.getValue());

    entity.setId(0);//protection
    Item item = itemRepository.save(entity);

    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.INSERT,
        item.getDataChangeCreatedBy());

    return item;
  }

  @Transactional
  public Item update(Item item) {
    checkItemValueLength(item.getValue());
    Item managedItem = itemRepository.findOne(item.getId());
    BeanUtils.copyEntityProperties(item, managedItem);
    managedItem = itemRepository.save(managedItem);

    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
        managedItem.getDataChangeLastModifiedBy());

    return managedItem;
  }

  private boolean checkItemValueLength(String value){
    int lengthLimit = Integer.valueOf(serverConfigService.getValue("item.value.length.limit", "20000"));
    if (!StringUtils.isEmpty(value) && value.length() > lengthLimit){
      throw new BadRequestException("value too long. length limit:" + lengthLimit);
    }
    return true;
  }

  private boolean checkItemKeyLength(String key){
    int lengthLimit = Integer.valueOf(serverConfigService.getValue("item.key.length.limit", "128"));
    if (!StringUtils.isEmpty(key) && key.length() > lengthLimit){
      throw new BadRequestException("key too long. length limit:" + lengthLimit);
    }
    return true;
  }

}
