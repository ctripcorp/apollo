package com.ctrip.framework.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.biz.utils.ConfigChangeContentBuilder;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.utils.StringUtils;


@Service
public class ItemSetService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private AuditService auditService;

  @Autowired
  private CommitService commitService;


  @Transactional
  public ItemChangeSets updateSet(String appId, String clusterName,
                                  String namespaceName, ItemChangeSets changeSet) {
    String operator = changeSet.getDataChangeLastModifiedBy();
    ConfigChangeContentBuilder configChangeContentBuilder = new ConfigChangeContentBuilder();

    if (!CollectionUtils.isEmpty(changeSet.getCreateItems())) {
      for (ItemDTO item : changeSet.getCreateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setId(0);//protection
        entity.setDataChangeCreatedBy(operator);
        entity.setDataChangeLastModifiedBy(operator);
        Item createdItem = itemRepository.save(entity);
        configChangeContentBuilder.createItem(createdItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.INSERT, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getUpdateItems())) {
      for (ItemDTO item : changeSet.getUpdateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        Item managedItem = itemRepository.findOne(entity.getId());
        Item beforeUpdateItem = BeanUtils.transfrom(Item.class, managedItem);
        BeanUtils.copyEntityProperties(entity, managedItem);
        managedItem.setDataChangeLastModifiedBy(operator);
        Item updatedItem = itemRepository.save(managedItem);
        configChangeContentBuilder.updateItem(beforeUpdateItem, updatedItem);

      }
      auditService.audit("ItemSet", null, Audit.OP.UPDATE, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getDeleteItems())) {
      for (ItemDTO item : changeSet.getDeleteItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setDeleted(true);
        entity.setDataChangeLastModifiedBy(operator);
        Item deletedItem = itemRepository.save(entity);
        configChangeContentBuilder.deleteItem(deletedItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.DELETE, operator);
    }

    String configChangeContent = configChangeContentBuilder.build();
    if (!StringUtils.isEmpty(configChangeContent)){
      createCommit(appId, clusterName, namespaceName, configChangeContentBuilder.build(), changeSet.getDataChangeLastModifiedBy());
    }

    return changeSet;

  }

  private void createCommit(String appId, String clusterName, String namespaceName, String configChangeContent,  String operator){

    Commit commit = new Commit();
    commit.setAppId(appId);
    commit.setClusterName(clusterName);
    commit.setNamespaceName(namespaceName);
    commit.setChangeSets(configChangeContent);
    commit.setDataChangeCreatedBy(operator);
    commit.setDataChangeLastModifiedBy(operator);
    commitService.save(commit);
  }

}
