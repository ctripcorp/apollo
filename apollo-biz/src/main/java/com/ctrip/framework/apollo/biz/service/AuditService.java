package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.repository.AuditRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日志审计 Service
 */
@Service
public class AuditService {

  private final AuditRepository auditRepository;

  public AuditService(final AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  /**
   * 通过所有人查询日志审计信息列表.
   *
   * @param owner 所有人
   * @return 日志审计信息列表
   */
  List<Audit> findByOwner(String owner) {
    return auditRepository.findByOwner(owner);
  }

  /**
   * 通过所有人查询日志审计信息列表.
   *
   * @param owner  所有人
   * @param entity 操作表(实体信息)
   * @param op     操作类型
   * @return 日志审计信息列表
   */
  List<Audit> find(String owner, String entity, String op) {
    return auditRepository.findAudits(owner, entity, op);
  }

  /**
   * 记录日志审计信息
   *
   * @param entityName 实体名称
   * @param entityId   实体id
   * @param op         操作类型
   * @param owner      所有人
   */
   @Transactional(rollbackFor = Exception.class)
  void audit(String entityName, Long entityId, Audit.OP op, String owner) {
    Audit audit = new Audit();
    audit.setEntityName(entityName);
    audit.setEntityId(entityId);
    audit.setOpName(op.name());
    audit.setDataChangeCreatedBy(owner);
    auditRepository.save(audit);
  }

  /**
   * 保存日志审核信息
   *
   * @param audit 日志审核信息
   */
   @Transactional(rollbackFor = Exception.class)
  void audit(Audit audit) {
    auditRepository.save(audit);
  }
}
