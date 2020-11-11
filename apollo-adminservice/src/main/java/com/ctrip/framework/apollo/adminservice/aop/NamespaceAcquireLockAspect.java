package com.ctrip.framework.apollo.adminservice.aop;


import com.ctrip.framework.apollo.adminservice.controller.ItemController;
import com.ctrip.framework.apollo.adminservice.controller.ItemSetController;
import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


/**
 * 名称空间获取锁的切面
 * <p>
 * 一个namespace在一次发布中只能允许一个人修改配置</p>
 * <p>通过数据库lock表来实现</p>
 */
@Aspect
@Component
public class NamespaceAcquireLockAspect {

  private static final Logger logger = LoggerFactory.getLogger(NamespaceAcquireLockAspect.class);

  private final NamespaceLockService namespaceLockService;
  private final NamespaceService namespaceService;
  private final ItemService itemService;
  private final BizConfig bizConfig;

  public NamespaceAcquireLockAspect(
      final NamespaceLockService namespaceLockService,
      final NamespaceService namespaceService,
      final ItemService itemService,
      final BizConfig bizConfig) {
    this.namespaceLockService = namespaceLockService;
    this.namespaceService = namespaceService;
    this.itemService = itemService;
    this.bizConfig = bizConfig;
  }

  /**
   * 创建配置项之前必须要获取到锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param item          配置项
   * @see ItemController#create(String, String, String, ItemDTO)
   */
  @Before(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)", argNames = "appId,clusterName,namespaceName,item")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
      ItemDTO item) {
    acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
  }

  /**
   * 更新配置项之前必须要获取到锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemId        配置项id
   * @param item          配置项
   * @see ItemController#update(String, String, String, long, ItemDTO)
   */
  @Before(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)", argNames = "appId,clusterName,namespaceName,itemId,item")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
      ItemDTO item) {
    acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
  }

  /**
   * 更新配置项变化之前必须要获取到锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param changeSet     配置项变化的集合列表数据
   * @see ItemSetController#create(String, String, String, ItemChangeSets)
   */
  @Before(value = "@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)", argNames = "appId,clusterName,namespaceName,changeSet")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
      ItemChangeSets changeSet) {
    acquireLock(appId, clusterName, namespaceName, changeSet.getDataChangeLastModifiedBy());
  }

  /**
   * 删除配置项之前必须要获取到锁
   *
   * @param itemId   配置项id
   * @param operator 操作人
   * @see ItemController#delete(long, String)
   */
  @Before(value = "@annotation(PreAcquireNamespaceLock) && args(itemId, operator, ..)", argNames = "itemId,operator")
  public void requireLockAdvice(long itemId, String operator) {
    Item item = itemService.findOne(itemId);
    if (item == null) {
      throw new BadRequestException("item not exist.");
    }
    acquireLock(item.getNamespaceId(), operator);
  }

  /**
   * 获取锁
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param currentUser   当前用户
   */
  void acquireLock(String appId, String clusterName, String namespaceName,
      String currentUser) {
    // 关闭名称空间锁时直接退出
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    // 获取锁
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    acquireLock(namespace, currentUser);
  }

  /**
   * 获取锁
   *
   * @param namespaceId 名称空间id
   * @param currentUser 当前用户
   */
  void acquireLock(long namespaceId, String currentUser) {
    // 关闭名称空间锁时直接退出
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    // 获取锁
    Namespace namespace = namespaceService.findOne(namespaceId);
    acquireLock(namespace, currentUser);
  }

  /**
   * 获取锁
   *
   * @param namespace   名称空间信息
   * @param currentUser 当前用户
   */
  private void acquireLock(Namespace namespace, String currentUser) {
    if (namespace == null) {
      throw new BadRequestException("namespace not exist.");
    }
    long namespaceId = namespace.getId();

    // 获取名称空间锁
    NamespaceLock namespaceLock = namespaceLockService.findLock(namespaceId);
    if (namespaceLock == null) {
      try {
        // 锁成功
        tryLock(namespaceId, currentUser);
      } catch (DataIntegrityViolationException e) {
        // 锁失败
        namespaceLock = namespaceLockService.findLock(namespaceId);
        checkLock(namespace, namespaceLock, currentUser);
      } catch (Exception e) {
        logger.error("try lock error", e);
        throw e;
      }
    } else {
      // 锁存在时检查锁是否为当前用户创建
      checkLock(namespace, namespaceLock, currentUser);
    }
  }

  /**
   * 获取锁（加锁）
   *
   * @param namespaceId 名称空间id
   * @param user        用户名
   * @return 名称空间锁对象
   */
  private void tryLock(long namespaceId, String user) {
    NamespaceLock lock = new NamespaceLock();
    lock.setNamespaceId(namespaceId);
    lock.setDataChangeCreatedBy(user);
    lock.setDataChangeLastModifiedBy(user);
    namespaceLockService.tryLock(lock);
  }

  /**
   * 检查锁
   *
   * @param namespace     名称空间信息
   * @param namespaceLock 名称空间锁
   * @param currentUser   当前用户
   */
  private void checkLock(Namespace namespace, NamespaceLock namespaceLock,
      String currentUser) {
    // 锁为空直接抛出
    if (namespaceLock == null) {
      throw new ServiceException(
          String.format("Check lock for %s failed, please retry.", namespace.getNamespaceName()));
    }

    // 锁创建人与当前用户不匹配时抛出
    String lockOwner = namespaceLock.getDataChangeCreatedBy();
    if (!lockOwner.equals(currentUser)) {
      throw new BadRequestException(
          "namespace:" + namespace.getNamespaceName() + " is modified by " + lockOwner);
    }
  }


}
