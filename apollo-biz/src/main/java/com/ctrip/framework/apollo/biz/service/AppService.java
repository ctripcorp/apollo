package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.repository.AppRepository;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用 Service
 */
@Service
public class AppService {

  private final AppRepository appRepository;
  private final AuditService auditService;

  public AppService(final AppRepository appRepository, final AuditService auditService) {
    this.appRepository = appRepository;
    this.auditService = auditService;
  }

  /**
   * 判断应用id是滞唯一
   *
   * @param appId 应用id
   * @return true, 唯一.否则, false
   */
  public boolean isAppIdUnique(String appId) {
    Objects.requireNonNull(appId, "AppId must not be null");
    return Objects.isNull(appRepository.findByAppId(appId));
  }

  /**
   * 通过主键id删除应用信息
   *
   * @param id       主键id
   * @param operator 操作者
   */
  @Transactional(rollbackFor = Exception.class)
  public void delete(long id, String operator) {
    // 逻辑删除应用信息
    App app = appRepository.findById(id).orElse(null);
    if (app == null) {
      return;
    }

    app.setDeleted(true);
    app.setDataChangeLastModifiedBy(operator);
    appRepository.save(app);
    // 记录日志审计信息
    auditService.audit(App.class.getSimpleName(), id, Audit.OP.DELETE, operator);
  }

  /**
   * 获取全部应用信息
   *
   * @param pageable 分页对象
   * @return 所有的应用信息
   */
  public List<App> findAll(Pageable pageable) {
    Page<App> page = appRepository.findAll(pageable);
    return page.getContent();
  }

  /**
   * 通过应用名称获取应用信息列表
   *
   * @param name 应用名称
   * @return 应用信息列表
   */
  public List<App> findByName(String name) {
    return appRepository.findByName(name);
  }

  /**
   * 获取指定应用id的应用信息
   *
   * @param appId 指定的应用id
   * @return 指定应用id的应用信息
   */
  public App findOne(String appId) {
    return appRepository.findByAppId(appId);
  }

  /**
   * 保存应用信息
   *
   * @param entity 应用信息实体
   * @return 保存的应用信息
   */
  @Transactional(rollbackFor = Exception.class)
  public App save(App entity) {
    if (!isAppIdUnique(entity.getAppId())) {
      throw new ServiceException("appId not unique");
    }
    //protection
    entity.setId(0);
    App app = appRepository.save(entity);
    // 添加日志审计
    auditService.audit(App.class.getSimpleName(), app.getId(), Audit.OP.INSERT,
        app.getDataChangeCreatedBy());

    return app;
  }

  /**
   * 更新应用信息
   *
   * @param app 应用信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void update(App app) {
    String appId = app.getAppId();

    App managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }

    managedApp.setName(app.getName());
    managedApp.setOrgId(app.getOrgId());
    managedApp.setOrgName(app.getOrgName());
    managedApp.setOwnerName(app.getOwnerName());
    managedApp.setOwnerEmail(app.getOwnerEmail());
    managedApp.setDataChangeLastModifiedBy(app.getDataChangeLastModifiedBy());

    managedApp = appRepository.save(managedApp);

    // 添加日志审计
    auditService.audit(App.class.getSimpleName(), managedApp.getId(), Audit.OP.UPDATE,
        managedApp.getDataChangeLastModifiedBy());

  }
}
