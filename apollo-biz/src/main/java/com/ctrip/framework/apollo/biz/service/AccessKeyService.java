package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.AccessKey;
import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.repository.AccessKeyRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 访问密钥 Service层
 *
 * @author nisiyong
 */
@Service
public class AccessKeyService {

  /**
   * 访问密钥限制次数
   */
  private static final int ACCESSKEY_COUNT_LIMIT = 5;

  private final AccessKeyRepository accessKeyRepository;
  private final AuditService auditService;

  public AccessKeyService(
      AccessKeyRepository accessKeyRepository,
      AuditService auditService) {
    this.accessKeyRepository = accessKeyRepository;
    this.auditService = auditService;
  }

  /**
   * 通过应用id获取访问密钥列表
   *
   * @param appId 应用id
   * @return 访问密钥列表
   */
  public List<AccessKey> findByAppId(String appId) {
    return accessKeyRepository.findByAppId(appId);
  }

  /**
   * 创建访问密钥
   *
   * @param appId  应用id
   * @param entity 访问密钥实体
   * @return 访问密钥信息
   */
  @Transactional(rollbackFor = Exception.class)
  public AccessKey create(String appId, AccessKey entity) {
    long count = accessKeyRepository.countByAppId(appId);
    if (count >= ACCESSKEY_COUNT_LIMIT) {
      throw new BadRequestException("AccessKeys count limit exceeded");
    }

    entity.setId(0L);
    entity.setAppId(appId);
    entity.setDataChangeLastModifiedBy(entity.getDataChangeCreatedBy());

    // 保存访问密钥
    AccessKey accessKey = accessKeyRepository.save(entity);

    // 记录日志审计信息
    auditService.audit(AccessKey.class.getSimpleName(), accessKey.getId(), Audit.OP.INSERT,
        accessKey.getDataChangeCreatedBy());
    return accessKey;
  }

  /**
   * 更新访问密钥
   *
   * @param appId  应用id
   * @param entity 访问密钥实体
   * @return 访问密钥信息
   */
  @Transactional(rollbackFor = Exception.class)
  public AccessKey update(String appId, AccessKey entity) {
    long id = entity.getId();
    String operator = entity.getDataChangeLastModifiedBy();

    // 找到访问密钥并更改信息
    AccessKey accessKey = accessKeyRepository.findOneByAppIdAndId(appId, id);
    if (accessKey == null) {
      throw new BadRequestException("AccessKey not exist");
    }
    accessKey.setEnabled(entity.isEnabled());
    accessKey.setDataChangeLastModifiedBy(operator);
    accessKeyRepository.save(accessKey);

    // 记录日志审计信息
    auditService.audit(AccessKey.class.getSimpleName(), id, Audit.OP.UPDATE, operator);
    return accessKey;
  }

  /**
   * 删除访问密钥
   *
   * @param appId    应用id
   * @param id       访问密钥主键id
   * @param operator 操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void delete(String appId, long id, String operator) {
    // 找到访问密钥并删除（逻辑删除）
    AccessKey accessKey = accessKeyRepository.findOneByAppIdAndId(appId, id);
    if (accessKey == null) {
      throw new BadRequestException("AccessKey not exist");
    }
    if (accessKey.isEnabled()) {
      throw new BadRequestException("AccessKey should disable first");
    }

    accessKey.setDeleted(Boolean.TRUE);
    accessKey.setDataChangeLastModifiedBy(operator);
    accessKeyRepository.save(accessKey);

    // 记录日志审计信息
    auditService.audit(AccessKey.class.getSimpleName(), id, Audit.OP.DELETE, operator);
  }
}
