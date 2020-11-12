package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.AccessKeyDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.AccessKeyService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
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

  private final UserInfoHolder userInfoHolder;
  private final AccessKeyService accessKeyService;

  public AccessKeyController(
      UserInfoHolder userInfoHolder,
      AccessKeyService accessKeyService) {
    this.userInfoHolder = userInfoHolder;
    this.accessKeyService = accessKeyService;
  }

  /**
   * 指定应用下的指定环境添加密钥
   *
   * @param appId        应用id
   * @param env          环境
   * @param accessKeyDTO 访问密钥对象
   * @return 添加的密钥
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @PostMapping(value = "/apps/{appId}/envs/{env}/accesskeys")
  public AccessKeyDTO save(@PathVariable String appId, @PathVariable String env,
      @RequestBody AccessKeyDTO accessKeyDTO) {
    String secret = UUID.randomUUID().toString().replaceAll("-", "");
    accessKeyDTO.setAppId(appId);
    accessKeyDTO.setSecret(secret);
    return accessKeyService.createAccessKey(Env.valueOf(env), accessKeyDTO);
  }

  /**
   * 通过appId和环境对象 查询访问密钥列表
   *
   * @param appId 应用id
   * @param env   环境对象
   * @return 访问密钥列表
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @GetMapping(value = "/apps/{appId}/envs/{env}/accesskeys")
  public List<AccessKeyDTO> findByAppId(@PathVariable String appId,
      @PathVariable String env) {
    return accessKeyService.findByAppId(Env.valueOf(env), appId);
  }

  /**
   * 删除访问密钥
   *
   * @param env   环境
   * @param appId 应用id
   * @param id    访问密钥主键id
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @DeleteMapping(value = "/apps/{appId}/envs/{env}/accesskeys/{id}")
  public void delete(@PathVariable String appId, @PathVariable String env, @PathVariable long id) {
    String operator = userInfoHolder.getUser().getUserId();
    accessKeyService.deleteAccessKey(Env.valueOf(env), appId, id, operator);
  }

  /**
   * 开启访问密钥
   *
   * @param env   环境
   * @param appId 应用id
   * @param id    访问密钥id
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @PutMapping(value = "/apps/{appId}/envs/{env}/accesskeys/{id}/enable")
  public void enable(@PathVariable String appId, @PathVariable String env, @PathVariable long id) {
    String operator = userInfoHolder.getUser().getUserId();
    accessKeyService.enable(Env.valueOf(env), appId, id, operator);
  }

  /**
   * 关闭访问密钥
   *
   * @param env   环境
   * @param appId 应用id
   * @param id    访问密钥主键id
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @PutMapping(value = "/apps/{appId}/envs/{env}/accesskeys/{id}/disable")
  public void disable(@PathVariable String appId, @PathVariable String env, @PathVariable long id) {
    String operator = userInfoHolder.getUser().getUserId();
    accessKeyService.disable(Env.valueOf(env), appId, id, operator);
  }
}
