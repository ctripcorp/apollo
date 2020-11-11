package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.AccessKey;
import com.ctrip.framework.apollo.biz.service.AccessKeyService;
import com.ctrip.framework.apollo.common.dto.AccessKeyDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访问密钥 Controller层
 *
 * @author nisiyong
 */
@RestController
public class AccessKeyController {

  private final AccessKeyService accessKeyService;

  public AccessKeyController(
      AccessKeyService accessKeyService) {
    this.accessKeyService = accessKeyService;
  }

  /**
   * 创建访问密钥
   *
   * @param appId 应用id
   * @param dto   访问密钥DTO
   * @return 访问密钥信息
   */
  @PostMapping(value = "/apps/{appId}/accesskeys")
  public AccessKeyDTO create(@PathVariable String appId, @RequestBody AccessKeyDTO dto) {
    AccessKey entity = BeanUtils.transform(AccessKey.class, dto);
    entity = accessKeyService.create(appId, entity);
    return BeanUtils.transform(AccessKeyDTO.class, entity);
  }

  /**
   * 通过应用id获取访问密钥列表
   *
   * @param appId 应用id
   * @return 访问密钥列表
   */
  @GetMapping(value = "/apps/{appId}/accesskeys")
  public List<AccessKeyDTO> findByAppId(@PathVariable String appId) {
    List<AccessKey> accessKeyList = accessKeyService.findByAppId(appId);
    return BeanUtils.batchTransform(AccessKeyDTO.class, accessKeyList);
  }

  /**
   * 删除访问密钥
   *
   * @param appId    应用id
   * @param id       访问密钥主键id
   * @param operator 操作者
   */
  @DeleteMapping(value = "/apps/{appId}/accesskeys/{id}")
  public void delete(@PathVariable String appId, @PathVariable long id, String operator) {
    accessKeyService.delete(appId, id, operator);
  }

  /**
   * 开启访问密钥
   *
   * @param appId    应用id
   * @param id       访问密钥id
   * @param operator 操作者
   */
  @PutMapping(value = "/apps/{appId}/accesskeys/{id}/enable")
  public void enable(@PathVariable String appId, @PathVariable long id, String operator) {
    AccessKey entity = new AccessKey();
    entity.setId(id);
    entity.setEnabled(true);
    entity.setDataChangeLastModifiedBy(operator);
    accessKeyService.update(appId, entity);
  }

  /**
   * 关闭访问密钥
   *
   * @param appId    应用id
   * @param id       访问密钥主键id
   * @param operator 操作者
   */
  @PutMapping(value = "/apps/{appId}/accesskeys/{id}/disable")
  public void disable(@PathVariable String appId, @PathVariable long id, String operator) {
    AccessKey entity = new AccessKey();
    entity.setId(id);
    entity.setEnabled(false);
    entity.setDataChangeLastModifiedBy(operator);
    accessKeyService.update(appId, entity);
  }
}
