package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.utils.ConfigChangeContentBuilder;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service
public class ItemSetService {

  @Autowired
  private AuditService auditService;

  @Autowired
  private CommitService commitService;

  @Autowired
  private ItemService itemService;

  @Transactional
  public ItemChangeSets updateSet(Namespace namespace, ItemChangeSets changeSets){
    return updateSet(namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName(), changeSets);
  }

  @Transactional
  public ItemChangeSets updateSet(String appId, String clusterName,
                                  String namespaceName, ItemChangeSets changeSet) {
    String operator = changeSet.getDataChangeLastModifiedBy();
    ConfigChangeContentBuilder configChangeContentBuilder = new ConfigChangeContentBuilder();

    if (!CollectionUtils.isEmpty(changeSet.getCreateItems())) {
      for (ItemDTO item : changeSet.getCreateItems()) {
        Item entity = BeanUtils.transform(Item.class, item);
        entity.setDataChangeCreatedBy(operator);
        entity.setDataChangeLastModifiedBy(operator);
        Item createdItem = itemService.save(entity);
        configChangeContentBuilder.createItem(createdItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.INSERT, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getUpdateItems())) {
      for (ItemDTO item : changeSet.getUpdateItems()) {
        Item entity = BeanUtils.transform(Item.class, item);

        Item managedItem = itemService.findOne(entity.getId());
        if (managedItem == null) {
          throw new NotFoundException(String.format("item not found.(key=%s)", entity.getKey()));
        }
        Item beforeUpdateItem = BeanUtils.transform(Item.class, managedItem);

        //protect. only value,comment,lastModifiedBy,lineNum can be modified
        managedItem.setValue(entity.getValue());
        managedItem.setComment(entity.getComment());
        managedItem.setLineNum(entity.getLineNum());
        managedItem.setDataChangeLastModifiedBy(operator);

        Item updatedItem = itemService.update(managedItem);
        configChangeContentBuilder.updateItem(beforeUpdateItem, updatedItem);

      }
      auditService.audit("ItemSet", null, Audit.OP.UPDATE, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getDeleteItems())) {
      for (ItemDTO item : changeSet.getDeleteItems()) {
        Item deletedItem = itemService.delete(item.getId(), operator);
        configChangeContentBuilder.deleteItem(deletedItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.DELETE, operator);
    }

    if (configChangeContentBuilder.hasContent()){
      createCommit(appId, clusterName, namespaceName, configChangeContentBuilder.build(),
                   changeSet.getDataChangeLastModifiedBy());
    }

    return changeSet;

  }

  private void createCommit(String appId, String clusterName, String namespaceName, String configChangeContent,
                            String operator) {

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
