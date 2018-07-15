package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.service.AdminService;
import com.ctrip.framework.apollo.biz.service.AppService;
import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class AppController {

  @Autowired
  private AppService appService;

  @Autowired
  private AdminService adminService;

  @RequestMapping(path = "/apps", method = RequestMethod.POST)
  public AppDTO create(@RequestBody AppDTO dto) {
    if (!InputValidator.isValidClusterNamespace(dto.getAppId())) {
      throw new BadRequestException(String.format("AppId格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }
    App entity = BeanUtils.transfrom(App.class, dto);
    App managedEntity = appService.findOne(entity.getAppId());
    if (managedEntity != null) {
      throw new BadRequestException("app already exist.");
    }

    entity = adminService.createNewApp(entity);

    dto = BeanUtils.transfrom(AppDTO.class, entity);
    return dto;
  }

  @RequestMapping(value = "/apps/{appId:.+}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId, @RequestParam String operator) {
    App entity = appService.findOne(appId);
    if (entity == null) {
      throw new NotFoundException("app not found for appId " + appId);
    }
    appService.delete(entity.getId(), operator);
  }

  @RequestMapping(value = "/apps/{appId:.+}", method = RequestMethod.PUT)
  public void update(@PathVariable String appId, @RequestBody App app) {
    if (!Objects.equals(appId, app.getAppId())) {
      throw new BadRequestException("The App Id of path variable and request body is different");
    }

    appService.update(app);
  }

  @RequestMapping(value = "/apps", method = RequestMethod.GET)
  public List<AppDTO> find(@RequestParam(value = "name", required = false) String name,
                           Pageable pageable) {
    List<App> app = null;
    if (StringUtils.isBlank(name)) {
      app = appService.findAll(pageable);
    } else {
      app = appService.findByName(name);
    }
    return BeanUtils.batchTransform(AppDTO.class, app);
  }

  @RequestMapping(value = "/apps/{appId:.+}", method = RequestMethod.GET)
  public AppDTO get(@PathVariable("appId") String appId) {
    App app = appService.findOne(appId);
    if (app == null) {
      throw new NotFoundException("app not found for appId " + appId);
    }
    return BeanUtils.transfrom(AppDTO.class, app);
  }

  @RequestMapping(value = "/apps/{appId}/unique", method = RequestMethod.GET)
  public boolean isAppIdUnique(@PathVariable("appId") String appId) {
    return appService.isAppIdUnique(appId);
  }

  @RequestMapping(value = "/apps", method = RequestMethod.DELETE)
  public void deleteApp(@RequestParam("appId") String appId,
                        @RequestParam("operator") String operator) {
    App app = appService.findOne(appId);
    if (Objects.isNull(app)) {
      throw new NotFoundException("app not found for appId " + appId);
    }

    try {
      adminService.deleteApp(appId, operator);
    } catch (Exception e) {
      String exc = String
              .format("user:%s deleting app：%s,failure:%s", operator, app, e.getMessage());
      throw new ServiceException(exc);
    }
  }
}
