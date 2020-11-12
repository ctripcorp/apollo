package com.ctrip.framework.apollo.biz.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.EntityManagerFactoryAccessor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 实体管理工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@Component
public class EntityManagerUtil extends EntityManagerFactoryAccessor {

  /**
   * 关闭实体管理器。小心使用！这只用于异步请求，在异步请求完成之前Spring不会关闭实体管理器。
   */
  public void closeEntityManager() {
    // 获得 EntityManagerHolder 对象
    EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager
        .getResource(getEntityManagerFactory());
    if (emHolder == null) {
      return;
    }
    log.debug("Closing JPA EntityManager in EntityManagerUtil");
    // 关闭 EntityManager
    EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
  }
}
