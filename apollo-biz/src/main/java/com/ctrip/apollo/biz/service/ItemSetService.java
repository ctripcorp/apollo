package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;

import java.util.Date;

@Service
public class ItemSetService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private AuditService auditService;

  @Transactional
  public void updateSet(ItemChangeSets changeSet, String owner) {
    if (changeSet.getCreateItems() != null) {
      for (ItemDTO item : changeSet.getCreateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setDataChangeCreatedTime(new Date());
        entity.setDataChangeCreatedBy(owner);
        entity.setDataChangeLastModifiedBy(owner);
        itemRepository.save(entity);
      }
      auditService.audit("ItemSet", null, Audit.OP.INSERT, owner);
    }

    if (changeSet.getUpdateItems() != null) {
      for (ItemDTO item : changeSet.getUpdateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        Item managedItem = itemRepository.findOne(entity.getId());
        BeanUtils.copyEntityProperties(entity, managedItem);
        managedItem.setDataChangeLastModifiedBy(owner);
        itemRepository.save(managedItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.UPDATE, owner);
    }

    if (changeSet.getDeleteItems() != null) {
      for (ItemDTO item : changeSet.getDeleteItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setDataChangeLastModifiedBy(owner);
        itemRepository.save(entity);
        itemRepository.delete(item.getId());
      }
      auditService.audit("ItemSet", null, Audit.OP.DELETE, owner);
    }
  }
}
