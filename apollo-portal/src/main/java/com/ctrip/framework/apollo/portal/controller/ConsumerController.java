package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.openapi.entity.Consumer;
import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消费者(第三方) Controller
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class ConsumerController {

  private static final Date DEFAULT_EXPIRES = new GregorianCalendar(2099, Calendar.JANUARY, 1)
      .getTime();

  private final ConsumerService consumerService;

  public ConsumerController(final ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  /**
   * 创建消费者
   *
   * @param consumer 消费者信息
   * @param expires  过期时间
   * @return 创建的消费者
   */
  @Transactional(rollbackFor = Exception.class)
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping(value = "/consumers")
  public ConsumerToken createConsumer(@RequestBody Consumer consumer,
      @RequestParam(value = "expires", required = false)
      @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date
          expires) {
    // 校验非空
    if (com.ctrip.framework.apollo.core.utils.StringUtils.isContainEmpty(consumer.getAppId(),
        consumer.getName(), consumer.getOwnerName(), consumer.getOrgId())) {
      throw new BadRequestException("Params(appId、name、ownerName、orgId) can not be empty.");
    }
    // 创建 Consumer 对象，并保存到数据库中
    Consumer createdConsumer = consumerService.createConsumer(consumer);

    // 创建 ConsumerToken 对象，并保存到数据库中
    if (Objects.isNull(expires)) {
      expires = DEFAULT_EXPIRES;
    }
    return consumerService.generateAndSaveConsumerToken(createdConsumer, expires);
  }

  /**
   * 获取应用下的消费者授权令牌
   *
   * @param appId 应用id
   * @return 消费者授权令牌
   */
  @GetMapping(value = "/consumers/by-appId")
  public ConsumerToken getConsumerTokenByAppId(@RequestParam String appId) {
    return consumerService.getConsumerTokenByAppId(appId);
  }

  /**
   * 分配名称空间角色给消费者
   *
   * @param token     授权令牌
   * @param type      类型
   * @param envs      环境
   * @param namespace 名称空间
   * @return 消费者角色列表
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping(value = "/consumers/{token}/assign-role")
  public List<ConsumerRole> assignNamespaceRoleToConsumer(@PathVariable String token,
      @RequestParam String type, @RequestParam(required = false) String envs,
      @RequestBody NamespaceDTO namespace) {

    String appId = namespace.getAppId();
    String namespaceName = namespace.getNamespaceName();

    if (StringUtils.isBlank(appId)) {
      throw new BadRequestException("Params(AppId) can not be empty.");
    }
    if (Objects.equals("AppRole", type)) {
      return Collections.singletonList(consumerService.assignAppRoleToConsumer(token, appId));
    }
    if (StringUtils.isBlank(namespaceName)) {
      throw new BadRequestException("Params(NamespaceName) can not be empty.");
    }
    if (null != envs) {
      String[] envArray = envs.split(",");
      // 环境列表
      List<String> envList = Lists.newArrayList();
      // validate env parameter
      for (String env : envArray) {
        if (Strings.isNullOrEmpty(env)) {
          continue;
        }
        if (Env.UNKNOWN.equals(Env.transformEnv(env))) {
          throw new BadRequestException(String.format("env: %s is illegal", env));
        }
        envList.add(env);
      }

      List<ConsumerRole> consumeRoles = new ArrayList<>();
      // 多环境分配名称空间角色
      for (String env : envList) {
        consumeRoles.addAll(
            consumerService.assignNamespaceRoleToConsumer(token, appId, namespaceName, env));
      }
      return consumeRoles;
    }

    return consumerService.assignNamespaceRoleToConsumer(token, appId, namespaceName);
  }


}
