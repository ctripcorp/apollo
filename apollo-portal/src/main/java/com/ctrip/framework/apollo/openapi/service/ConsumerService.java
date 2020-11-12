package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.openapi.entity.Consumer;
import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.repository.ConsumerAuditRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerTokenRepository;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息者 Service层，提供 Consumer、ConsumerToken、ConsumerAudit、ConsumerRole 相关的 Service 逻辑
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerService {

  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat
      .getInstance("yyyyMMddHHmmss");
  private static final Joiner KEY_JOINER = Joiner.on("|");

  private final UserInfoHolder userInfoHolder;
  private final ConsumerTokenRepository consumerTokenRepository;
  private final ConsumerRepository consumerRepository;
  private final ConsumerAuditRepository consumerAuditRepository;
  private final ConsumerRoleRepository consumerRoleRepository;
  private final PortalConfig portalConfig;
  private final RolePermissionService rolePermissionService;
  private final UserService userService;

  /**
   * 创建 Consumer 对象
   *
   * @param userInfoHolder          用户令牌供应器
   * @param consumerTokenRepository 消费者tokenRepository
   * @param consumerRepository      消费者Repository
   * @param consumerAuditRepository 消费者审计Repository
   * @param consumerRoleRepository  消费者令牌Repository
   * @param portalConfig            界面配置
   * @param rolePermissionService   角色权限 Service
   * @param userService             用户Service
   */
  public ConsumerService(
      final UserInfoHolder userInfoHolder,
      final ConsumerTokenRepository consumerTokenRepository,
      final ConsumerRepository consumerRepository,
      final ConsumerAuditRepository consumerAuditRepository,
      final ConsumerRoleRepository consumerRoleRepository,
      final PortalConfig portalConfig,
      final RolePermissionService rolePermissionService,
      final UserService userService) {
    this.userInfoHolder = userInfoHolder;
    this.consumerTokenRepository = consumerTokenRepository;
    this.consumerRepository = consumerRepository;
    this.consumerAuditRepository = consumerAuditRepository;
    this.consumerRoleRepository = consumerRoleRepository;
    this.portalConfig = portalConfig;
    this.rolePermissionService = rolePermissionService;
    this.userService = userService;
  }

  /**
   * 创建消费者信息
   *
   * @param consumer 消费者信息
   * @return 创建的消费者信息
   */
  public Consumer createConsumer(Consumer consumer) {
    String appId = consumer.getAppId();

    // 找到指定应用的消费者
    Consumer managedConsumer = consumerRepository.findByAppId(appId);
    if (managedConsumer != null) {
      throw new BadRequestException("Consumer already exist");
    }

    String ownerName = consumer.getOwnerName();
    // 消费者所有人信息
    UserInfo owner = userService.findByUserId(ownerName);
    if (owner == null) {
      throw new BadRequestException(String.format("User does not exist. UserId = %s", ownerName));
    }
    // 设置 Consumer 的创建和最后修改人为当前管理员
    consumer.setOwnerEmail(owner.getEmail());
    String operator = userInfoHolder.getUser().getUserId();
    consumer.setDataChangeCreatedBy(operator);
    consumer.setDataChangeLastModifiedBy(operator);
    // 保存 Consumer 到数据库中
    return consumerRepository.save(consumer);
  }

  /**
   * 生成或者保存消费者授权token
   *
   * @param consumer 消息者信息
   * @param expires  过期时间
   * @return 消息者授权token
   */
  public ConsumerToken generateAndSaveConsumerToken(Consumer consumer, Date expires) {
    Preconditions.checkArgument(consumer != null, "Consumer can not be null");
    // 生成 ConsumerToken 对象
    ConsumerToken consumerToken = generateConsumerToken(consumer, expires);
    consumerToken.setId(0);
    // 保存 ConsumerToken 到数据库中
    return consumerTokenRepository.save(consumerToken);
  }

  /**
   * 获取应用下的消费者授权令牌
   *
   * @param appId 应用id
   * @return 消费者授权令牌
   */
  public ConsumerToken getConsumerTokenByAppId(String appId) {
    Consumer consumer = consumerRepository.findByAppId(appId);
    if (consumer == null) {
      return null;
    }

    return consumerTokenRepository.findByConsumerId(consumer.getId());
  }

  /**
   * 获得 Token 获得对应的消费者id
   *
   * @param token Token
   * @return 消费者id
   */
  public Long getConsumerIdByToken(String token) {
    if (Strings.isNullOrEmpty(token)) {
      return null;
    }
    ConsumerToken consumerToken = consumerTokenRepository.findTopByTokenAndExpiresAfter(token,
        new Date());
    return consumerToken == null ? null : consumerToken.getConsumerId();
  }

  /**
   * 获取消费者信息
   *
   * @param consumerId 消费者id
   * @return 消费者信息
   */
  public Consumer getConsumerByConsumerId(long consumerId) {
    return consumerRepository.findById(consumerId).orElse(null);
  }

  /**
   * 授权名称空间的角色给消费者
   *
   * @param token         访问令牌
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @return 消费者角色列表信息
   */
  public List<ConsumerRole> assignNamespaceRoleToConsumer(String token, String appId,
      String namespaceName) {
    return assignNamespaceRoleToConsumer(token, appId, namespaceName, null);
  }

  /**
   * 授权名称空间的角色给消费者
   *
   * @param token         访问令牌
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return 消费者角色列表信息
   */
  @Transactional(rollbackFor = Exception.class)
  public List<ConsumerRole> assignNamespaceRoleToConsumer(String token, String appId,
      String namespaceName, String env) {
    // 校验 Token 是否有对应的 Consumer 。若不存在，抛出 BadRequestException 异常
    Long consumerId = getConsumerIdByToken(token);
    if (consumerId == null) {
      throw new BadRequestException("Token is Illegal");
    }
    // 获得 Namespace 对应的 Role 们。若有任一不存在，抛出 BadRequestException 异常
    Role namespaceModifyRole = rolePermissionService
        .findRoleByRoleName(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName, env));
    Role namespaceReleaseRole = rolePermissionService
        .findRoleByRoleName(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName, env));

    if (namespaceModifyRole == null || namespaceReleaseRole == null) {
      throw new BadRequestException(
          "Namespace's role does not exist. Please check whether namespace has created.");
    }

    long namespaceModifyRoleId = namespaceModifyRole.getId();
    long namespaceReleaseRoleId = namespaceReleaseRole.getId();

    // 获得 Consumer 对应的 ConsumerRole 们。若都存在，返回 ConsumerRole 数组
    ConsumerRole managedModifyRole = consumerRoleRepository
        .findByConsumerIdAndRoleId(consumerId, namespaceModifyRoleId);
    ConsumerRole managedReleaseRole = consumerRoleRepository
        .findByConsumerIdAndRoleId(consumerId, namespaceReleaseRoleId);
    if (managedModifyRole != null && managedReleaseRole != null) {
      return Arrays.asList(managedModifyRole, managedReleaseRole);
    }

    // 创建 Consumer 对应的 ConsumerRole
    String operator = userInfoHolder.getUser().getUserId();
    ConsumerRole namespaceModifyConsumerRole = createConsumerRole(consumerId, namespaceModifyRoleId,
        operator);
    ConsumerRole namespaceReleaseConsumerRole = createConsumerRole(consumerId,
        namespaceReleaseRoleId, operator);

    // 保存 Consumer 对应的 ConsumerRole 们到数据库中
    ConsumerRole createdModifyConsumerRole = consumerRoleRepository
        .save(namespaceModifyConsumerRole);
    ConsumerRole createdReleaseConsumerRole = consumerRoleRepository
        .save(namespaceReleaseConsumerRole);

    // 返回 ConsumerRole 数组
    return Arrays.asList(createdModifyConsumerRole, createdReleaseConsumerRole);
  }

  /**
   * ，授权应用的的角色给消费者
   *
   * @param token 授权令牌
   * @param appId 应用id
   * @return 消费者角色信息
   */
  @Transactional(rollbackFor = Exception.class)
  public ConsumerRole assignAppRoleToConsumer(String token, String appId) {
    // 校验 Token 是否有对应的 Consumer 。若不存在，抛出 BadRequestException 异常
    Long consumerId = getConsumerIdByToken(token);
    if (consumerId == null) {
      throw new BadRequestException("Token is Illegal");
    }

    // 获得 App 对应的 Role 对象
    Role masterRole = rolePermissionService
        .findRoleByRoleName(RoleUtils.buildAppMasterRoleName(appId));
    if (masterRole == null) {
      throw new BadRequestException(
          "App's role does not exist. Please check whether app has created.");
    }

    // 获得 Consumer 对应的 ConsumerRole 对象。若已存在，返回 ConsumerRole 对象
    long roleId = masterRole.getId();
    ConsumerRole managedModifyRole = consumerRoleRepository
        .findByConsumerIdAndRoleId(consumerId, roleId);
    if (managedModifyRole != null) {
      return managedModifyRole;
    }

    // 创建 Consumer 对应的 ConsumerRole 对象
    String operator = userInfoHolder.getUser().getUserId();
    ConsumerRole consumerRole = createConsumerRole(consumerId, roleId, operator);
    // 保存 Consumer 对应的 ConsumerRole 对象
    return consumerRoleRepository.save(consumerRole);
  }

  /**
   * 创建消费者审计列表
   *
   * @param consumerAudits 消费者审计列表信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void createConsumerAudits(Iterable<ConsumerAudit> consumerAudits) {
    consumerAuditRepository.saveAll(consumerAudits);
  }

  /**
   * 创建消费者授权token
   *
   * @param entity 消费者授权token实体信息
   * @return 消费者授权token
   */
  @Transactional(rollbackFor = Exception.class)
  public ConsumerToken createConsumerToken(ConsumerToken entity) {
    entity.setId(0); //for protection
    return consumerTokenRepository.save(entity);
  }

  /**
   * 生成消费者授权token
   *
   * @param consumer 消费者
   * @param expires  过期时间
   * @return 生成的消费者授权token
   */
  private ConsumerToken generateConsumerToken(Consumer consumer, Date expires) {
    // 消费者id
    long consumerId = consumer.getId();
    // 用户id
    String createdBy = userInfoHolder.getUser().getUserId();
    Date createdTime = new Date();

    ConsumerToken consumerToken = new ConsumerToken();
    consumerToken.setConsumerId(consumerId);
    consumerToken.setExpires(expires);
    consumerToken.setDataChangeCreatedBy(createdBy);
    consumerToken.setDataChangeCreatedTime(createdTime);
    consumerToken.setDataChangeLastModifiedBy(createdBy);
    consumerToken.setDataChangeLastModifiedTime(createdTime);

    generateAndEnrichToken(consumer, consumerToken);

    // 生成的授权token对象
    return consumerToken;
  }

  /**
   * 生成并充实令牌
   *
   * @param consumer      消费者
   * @param consumerToken 消费者Token
   */
  void generateAndEnrichToken(Consumer consumer, ConsumerToken consumerToken) {

    Preconditions.checkArgument(consumer != null);

    // 设置创建时间
    if (consumerToken.getDataChangeCreatedTime() == null) {
      consumerToken.setDataChangeCreatedTime(new Date());
    }
    // 设置token
    consumerToken.setToken(generateToken(consumer.getAppId(), consumerToken
        .getDataChangeCreatedTime(), portalConfig.consumerTokenSalt()));
  }

  /**
   * 生成token
   *
   * @param consumerAppId     消费者应用id
   * @param generationTime    生成时间
   * @param consumerTokenSalt 消费者授权token盐
   * @return 生成的授权token字符串
   */
  String generateToken(String consumerAppId, Date generationTime, String
      consumerTokenSalt) {
    return Hashing.sha1().hashString(KEY_JOINER.join(consumerAppId, TIMESTAMP_FORMAT.format
        (generationTime), consumerTokenSalt), Charsets.UTF_8).toString();
  }

  /**
   * 创建消费者角色对象
   *
   * @param consumerId 消费者id
   * @param roleId     角色id
   * @param operator   操作者
   * @return 消费者角色对象
   */
  ConsumerRole createConsumerRole(Long consumerId, Long roleId, String operator) {
    ConsumerRole consumerRole = new ConsumerRole();

    consumerRole.setConsumerId(consumerId);
    consumerRole.setRoleId(roleId);
    consumerRole.setDataChangeCreatedBy(operator);
    consumerRole.setDataChangeLastModifiedBy(operator);

    return consumerRole;
  }

}
