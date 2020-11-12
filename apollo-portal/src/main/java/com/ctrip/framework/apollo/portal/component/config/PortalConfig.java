package com.ctrip.framework.apollo.portal.component.config;


import com.ctrip.framework.apollo.common.config.RefreshableConfig;
import com.ctrip.framework.apollo.common.config.RefreshablePropertySource;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.PortalDBPropertySource;
import com.ctrip.framework.apollo.portal.service.SystemRoleManagerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 界面（门户）配置
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@Component
public class PortalConfig extends RefreshableConfig {

  /**
   * gson
   */
  private Gson gson = new Gson();
  /**
   * 部门列表信息.
   */
  private static final Type ORGANIZATION = new TypeToken<List<Organization>>() {
  }.getType();

  /**
   * 元服务配置(meta servers config in "PortalDB.ServerConfig")
   */
  private static final Type META_SERVERS = new TypeToken<Map<String, String>>() {
  }.getType();
  /**
   * 界面（门户）数据库属性源
   */
  private final PortalDBPropertySource portalDBPropertySource;

  /**
   * 构建界面（门户）配置
   *
   * @param portalDBPropertySource 界面（门户）数据库属性源
   */
  public PortalConfig(final PortalDBPropertySource portalDBPropertySource) {
    this.portalDBPropertySource = portalDBPropertySource;
  }

  @Override
  public List<RefreshablePropertySource> getRefreshablePropertySources() {
    return Collections.singletonList(portalDBPropertySource);
  }

  // Level: important
  // Level: important

  /**
   * 获取可支持的环境列表
   *
   * @return 可支持的环境列表
   */
  public List<Env> portalSupportedEnvs() {
    String[] configurations = getArrayProperty("apollo.portal.envs",
        new String[]{"FAT", "UAT", "PRO"});
    List<Env> envs = Lists.newLinkedList();

    for (String env : configurations) {
      envs.add(Env.addEnvironment(env));
    }
    return envs;
  }

  /**
   * 获取各环境Meta Service列表
   *
   * @return 环境与其元服务器之间的关系。如果遇到异常，则为空
   */
  public Map<String, String> getMetaServers() {
    final String key = "apollo.portal.meta.servers";
    String jsonContent = getValue(key);
    if (null == jsonContent) {
      return Collections.emptyMap();
    }

    // 注意内容的格式可能错误，从而导致异常
    Map<String, String> map = Collections.emptyMap();
    try {
      // 解析
      map = gson.fromJson(jsonContent, META_SERVERS);
    } catch (Exception e) {
      log.error("Wrong format for: {}", key, e);
    }
    return map;
  }

  /**
   * 获取超级管理员名称
   *
   * @return 用户身份标识(用户名)集合
   */
  public List<String> superAdmins() {
    String superAdminConfig = getValue("superAdmin", "");
    if (StringUtils.isBlank(superAdminConfig)) {
      return Collections.emptyList();
    }
    return splitter.splitToList(superAdminConfig);
  }

  /**
   * 获取邮件支持的环境
   *
   * @return 环境列表
   */
  public Set<Env> emailSupportedEnvs() {
    String[] configurations = getArrayProperty("email.supported.envs", null);

    Set<Env> result = Sets.newHashSet();
    if (ArrayUtils.isEmpty(configurations)) {
      return result;
    }

    for (String env : configurations) {
      result.add(Env.valueOf(env));
    }

    return result;
  }

  public Set<Env> webHookSupportedEnvs() {
    String[] configurations = getArrayProperty("webhook.supported.envs", null);

    Set<Env> result = Sets.newHashSet();
    if (configurations == null || configurations.length == 0) {
      return result;
    }

    for (String env : configurations) {
      result.add(Env.valueOf(env));
    }

    return result;
  }

  /**
   * 是否对指定环境显示配置信息
   *
   * @param env 环境字符串
   * @return true, 对指定环境显示配置信息,false,反之
   */
  public boolean isConfigViewMemberOnly(String env) {
    String[] configViewMemberOnlyEnvs = getArrayProperty("configView.memberOnly.envs",
        new String[0]);

    for (String memberOnlyEnv : configViewMemberOnlyEnvs) {
      if (memberOnlyEnv.equalsIgnoreCase(env)) {
        return true;
      }
    }

    return false;
  }

  // Level: normal

  /**
   * 连接超时时间
   *
   * @return 连接超时时间
   */
  public int connectTimeout() {
    return getIntProperty("api.connectTimeout", (int) TimeUnit.SECONDS.toMillis(3));
  }

  /**
   * 服务器读取到可用资源超时时间
   *
   * @return 服务务器读取到可用资源超时时间
   */
  public int readTimeout() {
    return getIntProperty("api.readTimeout", (int) TimeUnit.SECONDS.toMillis(10));
  }

  /**
   * 获取配置中的部门列表.
   *
   * @return 部门列表
   */
  public List<Organization> organizations() {

    String organizations = getValue("organizations");
    return organizations == null ? Collections.emptyList()
        : gson.fromJson(organizations, ORGANIZATION);
  }

  /**
   * 获取配置的服务地址(Apollo Portal的地址。方便用户从邮件点击跳转到Apollo Portal查看详细的发布信息)
   *
   * @return 配置的服务地址
   */
  public String portalAddress() {
    return getValue("apollo.portal.address");
  }

  /**
   * 是否允许紧急发布
   *
   * @param env 指定的环境
   * @return 指定环境允许紧急发布，true,否则，false
   */
  public boolean isEmergencyPublishAllowed(Env env) {
    String targetEnv = env.getName();

    String[] emergencyPublishSupportedEnvs = getArrayProperty("emergencyPublish.supported.envs",
        new String[0]);

    for (String supportedEnv : emergencyPublishSupportedEnvs) {
      if (Objects.equals(targetEnv, supportedEnv.toUpperCase().trim())) {
        return true;
      }
    }

    return false;
  }

  /***
   * Level: low
   **/
  /**
   * 获取名称空间发布提示支持的环境
   *
   * @return 获取名称空间发布提示支持的环境
   */
  public Set<Env> publishTipsSupportedEnvs() {
    String[] configurations = getArrayProperty("namespace.publish.tips.supported.envs", null);

    Set<Env> result = Sets.newHashSet();
    if (ArrayUtils.isEmpty(configurations)) {
      return result;
    }

    for (String env : configurations) {
      result.add(Env.valueOf(env));
    }

    return result;
  }

  /**
   * 得到消费者token的盐
   *
   * @return 消费者token的盐
   */
  public String consumerTokenSalt() {
    return getValue("consumer.token.salt", "apollo-portal");
  }

  /**
   * 获取邮件的发送人
   *
   * @return 邮件的发送人
   */
  public String emailSender() {
    return getValue("email.sender");
  }

  /**
   * 获取邮件内容模板框架。将邮件内容模板化、可配置化，方便管理和变更邮件内容
   *
   * @return 邮件内容模板框架。将邮件内容模板化、可配置化，方便管理和变更邮件内容
   */
  public String emailTemplateFramework() {
    return getValue("email.template.framework", "");
  }

  /**
   * 获取发布邮件的diff模块
   *
   * @return 发布邮件的diff模块
   */
  public String emailReleaseDiffModuleTemplate() {
    return getValue("email.template.release.module.diff", "");
  }

  /**
   * 获取回滚邮件的diff模块
   *
   * @return 回滚邮件的diff模块
   */
  public String emailRollbackDiffModuleTemplate() {
    return getValue("email.template.rollback.module.diff", "");
  }

  /**
   * 获取灰度发布的灰度规则模块模板.
   *
   * @return 灰度发布的灰度规则模块模板
   */
  public String emailGrayRulesModuleTemplate() {
    return getValue("email.template.release.module.rules", "");
  }

  /**
   * 获取wiki的地址.
   *
   * @return wiki的地址
   */
  public String wikiAddress() {
    return getValue("wiki.address", "https://github.com/ctripcorp/apollo/wiki");
  }

  /**
   * 是否允许项目管理员创建私有namespace
   *
   * @return true, 允许，否则,false
   */
  public boolean canAppAdminCreatePrivateNamespace() {
    return getBooleanProperty("admin.createPrivateNamespace.switch", true);
  }

  /**
   * 是否限制只有超级管理员和拥有创建应用权限的帐号可以创建项目
   *
   * @return true，限制，false，不限制
   */
  public boolean isCreateApplicationPermissionEnabled() {
    return getBooleanProperty(SystemRoleManagerService.CREATE_APPLICATION_LIMIT_SWITCH_KEY, false);
  }

  /**
   * 是否限制只有超级管理员和拥有管理员分配权限的帐号可以修改项目的Key
   *
   * @return true，限制，false，不限制
   */
  public boolean isManageAppMasterPermissionEnabled() {
    return getBooleanProperty(SystemRoleManagerService.MANAGE_APP_MASTER_LIMIT_SWITCH_KEY, false);
  }

  /**
   * 获取访问系统服务的Token集（多个用逗号分隔）
   *
   * @return 访问系统服务的Token集
   */
  public String getAdminServiceAccessTokens() {
    return getValue("admin-service.access.tokens");
  }

  /***
   * The following configurations are used in ctrip profile
   **/

  public int appId() {
    return getIntProperty("ctrip.appid", 0);
  }

  //send code & template id. apply from ewatch
  public String sendCode() {
    return getValue("ctrip.email.send.code");
  }

  public int templateId() {
    return getIntProperty("ctrip.email.template.id", 0);
  }

  /**
   * 电子邮件服务器队列中的电子邮件保留时间，单位为小时
   */
  public int survivalDuration() {
    return getIntProperty("ctrip.email.survival.duration", 5);
  }

  public boolean isSendEmailAsync() {
    return getBooleanProperty("email.send.async", true);
  }

  public String portalServerName() {
    return getValue("serverName");
  }

  public String casServerLoginUrl() {
    return getValue("casServerLoginUrl");
  }

  public String casServerUrlPrefix() {
    return getValue("casServerUrlPrefix");
  }

  public String credisServiceUrl() {
    return getValue("credisServiceUrl");
  }

  public String userServiceUrl() {
    return getValue("userService.url");
  }

  public String userServiceAccessToken() {
    return getValue("userService.accessToken");
  }

  public String soaServerAddress() {
    return getValue("soa.server.address");
  }

  public String cloggingUrl() {
    return getValue("clogging.server.url");
  }

  public String cloggingPort() {
    return getValue("clogging.server.port");
  }

  public String hermesServerAddress() {
    return getValue("hermes.server.address");
  }

  public String[] webHookUrls() {
    return getArrayProperty("config.release.webhook.service.url", null);
  }
}
