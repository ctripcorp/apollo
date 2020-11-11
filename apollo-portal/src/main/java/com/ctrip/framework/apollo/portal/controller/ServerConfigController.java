package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置中心本身需要一些配置,这些配置放在数据库里面
 */
@RestController
public class ServerConfigController {

  private final ServerConfigRepository serverConfigRepository;
  private final UserInfoHolder userInfoHolder;

  public ServerConfigController(final ServerConfigRepository serverConfigRepository,
      final UserInfoHolder userInfoHolder) {
    this.serverConfigRepository = serverConfigRepository;
    this.userInfoHolder = userInfoHolder;
  }

  /**
   * 创建或更新服务配置信息.
   *
   * @param serverConfig 服务配置
   * @return 服务配置信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping("/server/config")
  public ServerConfig createOrUpdate(@Valid @RequestBody ServerConfig serverConfig) {
    String modifiedBy = userInfoHolder.getUser().getUserId();

    // 存储的配置信息
    ServerConfig storedConfig = serverConfigRepository.findByKey(serverConfig.getKey());

    // 创建
    if (Objects.isNull(storedConfig)) {
      serverConfig.setDataChangeCreatedBy(modifiedBy);
      serverConfig.setDataChangeLastModifiedBy(modifiedBy);
      //为空，设置ID 为0，jpa执行新增操作
      serverConfig.setId(0L);
      return serverConfigRepository.save(serverConfig);
    }
    // 更新
    BeanUtils.copyEntityProperties(serverConfig, storedConfig);
    storedConfig.setDataChangeLastModifiedBy(modifiedBy);
    return serverConfigRepository.save(storedConfig);
  }

  /**
   * 加载服务配置信息.
   *
   * @param key 配置key
   * @return 服务配置信息
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping("/server/config/{key:.+}")
  public ServerConfig loadServerConfig(@PathVariable String key) {
    return serverConfigRepository.findByKey(key);
  }

}
