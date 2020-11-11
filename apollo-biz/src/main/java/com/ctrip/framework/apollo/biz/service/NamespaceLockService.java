package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.repository.NamespaceLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 名称空间锁 Service
 */
@Service
public class NamespaceLockService {

  private final NamespaceLockRepository namespaceLockRepository;

  public NamespaceLockService(final NamespaceLockRepository namespaceLockRepository) {
    this.namespaceLockRepository = namespaceLockRepository;
  }

  /**
   * 通过名称空间id获取名称空间锁对象
   *
   * @param namespaceId 名称空间id
   * @return 名称空间锁对象
   */
  public NamespaceLock findLock(Long namespaceId) {
    return namespaceLockRepository.findByNamespaceId(namespaceId);
  }

  /**
   * 加锁
   *
   * @param lock 名称空间锁对象
   * @return 名称空间锁对象
   */
   @Transactional(rollbackFor = Exception.class)
  public NamespaceLock tryLock(NamespaceLock lock) {
    return namespaceLockRepository.save(lock);
  }

  /**
   * 解锁
   *
   * @param namespaceId 名称空间锁id
   */
   @Transactional(rollbackFor = Exception.class)
  public void unlock(Long namespaceId) {
    namespaceLockRepository.deleteByNamespaceId(namespaceId);
  }
}
