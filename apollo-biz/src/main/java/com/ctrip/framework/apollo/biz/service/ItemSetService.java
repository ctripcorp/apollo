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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 配置项创建、更新、删除的列表数据 Service层
 */
@Service
public class ItemSetService {

  private final AuditService auditService;
  private final CommitService commitService;
  private final ItemService itemService;

  public ItemSetService(
      final AuditService auditService,
      final CommitService commitService,
      final ItemService itemService) {
    this.auditService = auditService;
    this.commitService = commitService;
    this.itemService = itemService;
  }

  /**
   * 更新配置项
   *
   * @param namespace  名称空间
   * @param changeSets 改变的配置项
   * @return 配置项创建、更新、删除的列表数据
   */
   @Transactional(rollbackFor = Exception.class)
  public ItemChangeSets updateSet(Namespace namespace, ItemChangeSets changeSets) {
    return updateSet(namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName(),
        changeSets);
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param changeSet     改变的配置项
   * @return 配置项创建、更新、删除的列表数据
   */
   @Transactional(rollbackFor = Exception.class)
  public ItemChangeSets updateSet(String appId, String clusterName,
      String namespaceName, ItemChangeSets changeSet) {

    String operator = changeSet.getDataChangeLastModifiedBy();
    ConfigChangeContentBuilder configChangeContentBuilder = new ConfigChangeContentBuilder();

    if (!CollectionUtils.isEmpty(changeSet.getCreateItems())) {
      // 构建创建的配置项
      for (ItemDTO item : changeSet.getCreateItems()) {
        Item entity = BeanUtils.transform(Item.class, item);
        entity.setDataChangeCreatedBy(operator);
        entity.setDataChangeLastModifiedBy(operator);

        //保存配置项
        Item createdItem = itemService.save(entity);
        configChangeContentBuilder.createItem(createdItem);
      }
      // 记录日志审计信息
      auditService.audit("ItemSet", null, Audit.OP.INSERT, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getUpdateItems())) {
      // 构建更新的配置项
      for (ItemDTO item : changeSet.getUpdateItems()) {
        Item entity = BeanUtils.transform(Item.class, item);

        Item managedItem = itemService.findOne(entity.getId());
        if (managedItem == null) {
          throw new NotFoundException(String.format("item not found.(key=%s)", entity.getKey()));
        }
        // 更新之前的配置项
        Item beforeUpdateItem = BeanUtils.transform(Item.class, managedItem);

        // 只有value,comment,lastModifiedBy,lineNum可以被修改
        managedItem.setValue(entity.getValue());
        managedItem.setComment(entity.getComment());
        managedItem.setLineNum(entity.getLineNum());
        managedItem.setDataChangeLastModifiedBy(operator);

        //更新配置项
        Item updatedItem = itemService.update(managedItem);
        configChangeContentBuilder.updateItem(beforeUpdateItem, updatedItem);

      }
      // 记录日志审计信息
      auditService.audit("ItemSet", null, Audit.OP.UPDATE, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getDeleteItems())) {
      for (ItemDTO item : changeSet.getDeleteItems()) {
        // 删除配置项
        Item deletedItem = itemService.delete(item.getId(), operator);
        configChangeContentBuilder.deleteItem(deletedItem);
      }
      // 记录日志审计信息
      auditService.audit("ItemSet", null, Audit.OP.DELETE, operator);
    }

    // 如果配置项有内容，保存提交信息
    if (configChangeContentBuilder.hasContent()) {
      createCommit(appId, clusterName, namespaceName, configChangeContentBuilder.build(),
          changeSet.getDataChangeLastModifiedBy());
    }
    return changeSet;
  }

  /**
   * 创建提交信息
   *
   * @param appId               应用id
   * @param clusterName         集群名称
   * @param namespaceName       名称空间名称
   * @param configChangeContent 配置改变内容
   * @param operator            操作者
   */
  private void createCommit(String appId, String clusterName, String namespaceName,
      String configChangeContent, String operator) {

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
