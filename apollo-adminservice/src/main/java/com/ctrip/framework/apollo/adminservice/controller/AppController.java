package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.service.AdminService;
import com.ctrip.framework.apollo.biz.service.AppService;
import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用 Controller.
 */
@RestController
public class AppController {

  private final AppService appService;
  private final AdminService adminService;

  public AppController(final AppService appService, final AdminService adminService) {
    this.appService = appService;
    this.adminService = adminService;
  }

  /**
   * 创建应用.
   *
   * @param dto 应用dto实体
   * @return 创建的应用
   */
  @PostMapping("/apps")
  public AppDTO create(@Valid @RequestBody AppDTO dto) {
    App entity = BeanUtils.transform(App.class, dto);
    //存在不创建
    App managedEntity = appService.findOne(entity.getAppId());
    if (managedEntity != null) {
      throw new BadRequestException("app already exist.");
    }

    // 创建新的应用
    entity = adminService.createNewApp(entity);
    return BeanUtils.transform(AppDTO.class, entity);
  }

  /**
   * 删除应用.
   *
   * @param appId    应用id
   * @param operator 操作者
   */
  @DeleteMapping("/apps/{appId:.+}")
  public void delete(@PathVariable("appId") String appId, @RequestParam String operator) {
    App entity = appService.findOne(appId);
    if (entity == null) {
      throw new NotFoundException("app not found for appId " + appId);
    }
    adminService.deleteApp(entity, operator);
  }

  /**
   * 更新应用.
   *
   * @param appId 指定的应用id
   * @param app   应用更新数据
   */
  @PutMapping("/apps/{appId:.+}")
  public void update(@PathVariable String appId, @RequestBody App app) {
    if (!Objects.equals(appId, app.getAppId())) {
      throw new BadRequestException("The App Id of path variable and request body is different");
    }
    appService.update(app);
  }

  /**
   * 通过应用名称查询应用列表.
   *
   * @param name     应用名称
   * @param pageable 分页
   * @return 应用信息列表
   */
  @GetMapping("/apps")
  public List<AppDTO> find(@RequestParam(value = "name", required = false) String name,
      Pageable pageable) {
    List<App> app;
    if (StringUtils.isBlank(name)) {
      app = appService.findAll(pageable);
    } else {
      app = appService.findByName(name);
    }
    return BeanUtils.batchTransform(AppDTO.class, app);
  }

  /**
   * 通过应用id获取应用.
   *
   * @param appId 应用id
   * @return 应用信息
   */
  @GetMapping("/apps/{appId:.+}")
  public AppDTO get(@PathVariable("appId") String appId) {
    App app = appService.findOne(appId);
    if (app == null) {
      throw new NotFoundException("app not found for appId " + appId);
    }
    return BeanUtils.transform(AppDTO.class, app);
  }

  /**
   * 检查应用id是否唯一.
   *
   * @param appId 应用id
   * @return true, 表示唯一，否则，false
   */
  @GetMapping("/apps/{appId}/unique")
  public boolean isAppIdUnique(@PathVariable("appId") String appId) {
    return appService.isAppIdUnique(appId);
  }
}
