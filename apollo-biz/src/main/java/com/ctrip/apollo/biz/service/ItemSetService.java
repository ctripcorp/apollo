package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;

import java.util.Date;

@Service
public class ItemSetService {

  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private NamespaceRepository namespaceRepository;

  public void updateSet(String appId, String clusterName, String namespaceName, ItemChangeSets changeSet) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName, namespaceName);

    String modifyBy = changeSet.getModifyBy();
    for (ItemDTO item : changeSet.getCreateItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      entity.setNamespaceId(namespace.getId());
      entity.setDataChangeCreatedBy(modifyBy);
      entity.setDataChangeCreatedTime(new Date());
      entity.setDataChangeLastModifiedTime(new Date());
      entity.setDataChangeLastModifiedBy(modifyBy);
      itemRepository.save(entity);
    }

    for (ItemDTO item : changeSet.getUpdateItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      Item managedItem = itemRepository.findOne(entity.getId());
      if (managedItem != null){
        managedItem.setLineNum(entity.getLineNum());
        managedItem.setComment(entity.getComment());
        managedItem.setValue(entity.getValue());
        managedItem.setDataChangeLastModifiedBy(modifyBy);
        managedItem.setDataChangeLastModifiedTime(new Date());
      }
      itemRepository.save(managedItem);
    }

    for (ItemDTO item : changeSet.getDeletedItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      entity.setDataChangeLastModifiedBy(modifyBy);
      itemRepository.delete(entity.getId());
    }
  }
}
