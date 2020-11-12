package com.ctrip.framework.apollo.portal.component.emailbuilder;


import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.common.constants.ReleaseOperationContext;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.Change;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * 配置发布邮箱构建器
 */
public abstract class ConfigPublishEmailBuilder {

  private static final String EMERGENCY_PUBLISH_TAG = "<span style='color:red'>(紧急发布)</span>";

  // 电子邮件内容公用字段占位符
  /**
   * 电子邮件应用id字段
   */
  private static final String EMAIL_CONTENT_FIELD_APPID = "#\\{appId\\}";
  /**
   * 电子邮件环境字段
   */
  private static final String EMAIL_CONTENT_FIELD_ENV = "#\\{env}";
  /**
   * 电子邮件集群名称字段
   */
  private static final String EMAIL_CONTENT_FIELD_CLUSTER = "#\\{clusterName}";
  /**
   * 电子邮件名称空间字段
   */
  private static final String EMAIL_CONTENT_FIELD_NAMESPACE = "#\\{namespaceName}";
  /**
   * 电子邮件操作者字段
   */
  private static final String EMAIL_CONTENT_FIELD_OPERATOR = "#\\{operator}";
  /**
   * 电子邮件发布时间字段
   */
  private static final String EMAIL_CONTENT_FIELD_RELEASE_TIME = "#\\{releaseTime}";
  /**
   * 电子邮件发布id字段
   */
  private static final String EMAIL_CONTENT_FIELD_RELEASE_ID = "#\\{releaseId}";
  /**
   * 电子邮件发布历史id字段
   */
  private static final String EMAIL_CONTENT_FIELD_RELEASE_HISTORY_ID = "#\\{releaseHistoryId}";
  /**
   * 电子邮件发布标题字段
   */
  private static final String EMAIL_CONTENT_FIELD_RELEASE_TITLE = "#\\{releaseTitle}";
  /**
   * 电子邮件发布备注字段
   */
  private static final String EMAIL_CONTENT_FIELD_RELEASE_COMMENT = "#\\{releaseComment}";
  /**
   * 电子邮件界面地址字段
   */
  private static final String EMAIL_CONTENT_FIELD_APOLLO_SERVER_ADDRESS = "#\\{apollo.portal.address}";
  /**
   * 电子邮件差异内容字段
   */
  private static final String EMAIL_CONTENT_FIELD_DIFF_CONTENT = "#\\{diffContent}";
  /**
   * 电子邮件紧急发布字段
   */
  private static final String EMAIL_CONTENT_FIELD_EMERGENCY_PUBLISH = "#\\{emergencyPublish}";
  /**
   * 电子邮件差异模块字段
   */

  private static final String EMAIL_CONTENT_DIFF_MODULE = "#\\{diffModule}";
  /**
   * 电子邮件灰度规则模块字段
   */
  protected static final String EMAIL_CONTENT_GRAY_RULES_MODULE = "#\\{rulesModule}";
  /**
   * 电子邮件灰度规则内容字段,电子邮件内容特殊字段占位符
   */
  protected static final String EMAIL_CONTENT_GRAY_RULES_CONTENT = "#\\{rulesContent}";
  /**
   * 值最大长度字段,设置配置的值max length以保护email
   */
  protected static final int VALUE_MAX_LENGTH = 100;
  /**
   * 时间格式
   */
  protected FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");


  @Autowired
  private RolePermissionService rolePermissionService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private UserService userService;
  @Autowired
  protected PortalConfig portalConfig;

  /**
   * email主题
   */
  protected abstract String subject();

  /**
   * 邮箱内容
   */
  protected abstract String emailContent(Env env, ReleaseHistoryBO releaseHistory);

  /**
   * 电子邮件body模板框架
   */
  protected abstract String getTemplateFramework();

  /**
   * 电子邮件body差异模块模板
   */
  protected abstract String getDiffModuleTemplate();

  /**
   * 构建邮箱信息
   *
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 邮件对象信息
   */

  public Email build(Env env, ReleaseHistoryBO releaseHistory) {

    Email email = new Email();

    email.setSubject(subject());
    // 发起人
    email.setSenderEmailAddress(portalConfig.emailSender());
    // 收件人
    email.setRecipients(recipients(releaseHistory.getAppId(), releaseHistory.getNamespaceName(),
        env.toString()));
    // 邮件内容
    String emailBody = emailContent(env, releaseHistory);
    // 清除未使用模块
    emailBody = emailBody.replaceAll(EMAIL_CONTENT_DIFF_MODULE, "");
    emailBody = emailBody.replaceAll(EMAIL_CONTENT_GRAY_RULES_MODULE, "");
    email.setBody(emailBody);

    return email;
  }

  /**
   * 获取 邮件公有内容
   *
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 邮件公有内容
   */
  protected String renderEmailCommonContent(Env env, ReleaseHistoryBO releaseHistory) {
    String template = getTemplateFramework();
    String renderResult = renderReleaseBasicInfo(template, env, releaseHistory);
    renderResult = renderDiffModule(renderResult, env, releaseHistory);
    return renderResult;
  }

  /**
   * 提供发布基本信息
   *
   * @param template       模板
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 提供发布基本信息
   */
  private String renderReleaseBasicInfo(String template, Env env, ReleaseHistoryBO releaseHistory) {
    String renderResult = template;

    Map<String, Object> operationContext = releaseHistory.getOperationContext();
    // 是否紧急发布
    boolean isEmergencyPublish =
        operationContext.containsKey(ReleaseOperationContext.IS_EMERGENCY_PUBLISH) &&
            (boolean) operationContext.get(ReleaseOperationContext.IS_EMERGENCY_PUBLISH);
    if (isEmergencyPublish) {
      renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_EMERGENCY_PUBLISH,
          Matcher.quoteReplacement(EMERGENCY_PUBLISH_TAG));
    } else {
      renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_EMERGENCY_PUBLISH, "");
    }

    // 字段替换
    renderResult = renderResult
        .replaceAll(EMAIL_CONTENT_FIELD_APPID, Matcher.quoteReplacement(releaseHistory.getAppId()));
    renderResult = renderResult
        .replaceAll(EMAIL_CONTENT_FIELD_ENV, Matcher.quoteReplacement(env.toString()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_CLUSTER, Matcher.quoteReplacement(
        releaseHistory.getClusterName()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_NAMESPACE, Matcher.quoteReplacement(
        releaseHistory.getNamespaceName()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_OPERATOR, Matcher.quoteReplacement(
        releaseHistory.getOperator()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_TITLE,
        Matcher.quoteReplacement(releaseHistory.getReleaseTitle()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_ID, String.valueOf(
        releaseHistory.getReleaseId()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_HISTORY_ID, String.valueOf(
        releaseHistory.getId()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_COMMENT, Matcher
        .quoteReplacement(releaseHistory.getReleaseComment() == null ? "" :
            releaseHistory.getReleaseComment()));
    renderResult = renderResult
        .replaceAll(EMAIL_CONTENT_FIELD_APOLLO_SERVER_ADDRESS, getApolloPortalAddress());
    return renderResult
        .replaceAll(EMAIL_CONTENT_FIELD_RELEASE_TIME,
            dateFormat.format(releaseHistory.getReleaseTime()));
  }

  /**
   * 提供差异模板
   *
   * @param bodyTemplate   内容模板
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 差异模板
   */
  private String renderDiffModule(String bodyTemplate, Env env, ReleaseHistoryBO releaseHistory) {
    String appId = releaseHistory.getAppId();
    String namespaceName = releaseHistory.getNamespaceName();

    // 应用名称空间历史
    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);
    if (appNamespace == null) {
      appNamespace = appNamespaceService.findPublicAppNamespace(namespaceName);
    }

    // 如果命名空间的格式不为“properties”，则不显示差异内容
    if (appNamespace == null ||
        !appNamespace.getFormat().equals(ConfigFileFormat.Properties.getValue())) {
      return bodyTemplate.replaceAll(EMAIL_CONTENT_DIFF_MODULE, "<br><h4>变更内容请点击链接到Apollo上查看</h4>");
    }

    ReleaseCompareResult result = getReleaseCompareResult(env, releaseHistory);

    if (!result.hasContent()) {
      return bodyTemplate.replaceAll(EMAIL_CONTENT_DIFF_MODULE, "<br><h4>无配置变更</h4>");
    }

    List<Change> changes = result.getChanges();
    StringBuilder changesHtmlBuilder = new StringBuilder();
    // 组装变更的信息
    for (Change change : changes) {
      String key = change.getEntity().getFirstEntity().getKey();
      String oldValue = change.getEntity().getFirstEntity().getValue();
      String newValue = change.getEntity().getSecondEntity().getValue();
      newValue = newValue == null ? "" : newValue;

      changesHtmlBuilder.append("<tr>");
      changesHtmlBuilder.append("<td width=\"10%\">").append(change.getType().toString())
          .append("</td>");
      changesHtmlBuilder.append("<td width=\"20%\">").append(cutOffString(key)).append("</td>");
      changesHtmlBuilder.append("<td width=\"35%\">").append(cutOffString(oldValue))
          .append("</td>");
      changesHtmlBuilder.append("<td width=\"35%\">").append(cutOffString(newValue))
          .append("</td>");

      changesHtmlBuilder.append("</tr>");
    }

    // 不同的内容
    String diffContent = Matcher.quoteReplacement(changesHtmlBuilder.toString());
    // 不同模块的模板
    String diffModuleTemplate = getDiffModuleTemplate();
    // 不同模块提花的结果
    String diffModuleRenderResult = diffModuleTemplate.replaceAll(EMAIL_CONTENT_FIELD_DIFF_CONTENT,
        diffContent);
    // 替换
    return bodyTemplate.replaceAll(EMAIL_CONTENT_DIFF_MODULE, diffModuleRenderResult);
  }

  /**
   * 发布信息比较结果
   *
   * @param env            环境
   * @param releaseHistory 发布历史
   * @return 发布信息比较结果
   */
  private ReleaseCompareResult getReleaseCompareResult(Env env, ReleaseHistoryBO releaseHistory) {
    // 如果是新的灰度发布
    if (releaseHistory.getOperation() == ReleaseOperation.GRAY_RELEASE
        && releaseHistory.getPreviousReleaseId() == 0) {

      // 最新的发布信息
      ReleaseDTO masterLatestActiveRelease = releaseService.loadLatestRelease(
          releaseHistory.getAppId(), env, releaseHistory.getClusterName(),
          releaseHistory.getNamespaceName());
      // 分支最新的信息
      ReleaseDTO branchLatestActiveRelease = releaseService
          .findReleaseById(env, releaseHistory.getReleaseId());

      // 比较
      return releaseService.compare(masterLatestActiveRelease, branchLatestActiveRelease);
    }

    // 比较
    return releaseService
        .compare(env, releaseHistory.getPreviousReleaseId(), releaseHistory.getReleaseId());
  }

  /**
   * 获取收件人列表
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param env           环境
   * @return 收件人列表
   */
  private List<String> recipients(String appId, String namespaceName, String env) {
    // 修改的用户列表
    Set<UserInfo> modifyRoleUsers = rolePermissionService.queryUsersWithRole(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE));
    // 环境修改角色用户列表
    Set<UserInfo> envModifyRoleUsers = rolePermissionService.queryUsersWithRole(RoleUtils
        .buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE, env));
    // 发布规则用户列表
    Set<UserInfo> releaseRoleUsers = rolePermissionService.queryUsersWithRole(
        RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE));
    // 环境发布角色用户列表
    Set<UserInfo> envReleaseRoleUsers = rolePermissionService.queryUsersWithRole(RoleUtils
        .buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE, env));
    // 所有人列表
    Set<UserInfo> owners = rolePermissionService
        .queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));

    // 用户id列表
    Set<String> userIds = new HashSet<>(
        modifyRoleUsers.size() + releaseRoleUsers.size() + owners.size());

    for (UserInfo userInfo : modifyRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : envModifyRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : releaseRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : envReleaseRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : owners) {
      userIds.add(userInfo.getUserId());
    }

    // 获取用户信息
    List<UserInfo> userInfos = userService.findByUserIds(Lists.newArrayList(userIds));

    if (CollectionUtils.isEmpty(userInfos)) {
      return Collections.emptyList();
    }

    // 获取用户邮箱
    List<String> recipients = new ArrayList<>(userInfos.size());
    for (UserInfo userInfo : userInfos) {
      recipients.add(userInfo.getEmail());
    }

    return recipients;
  }

  /**
   * 获取页面地址
   *
   * @return 页面地址
   */
  protected String getApolloPortalAddress() {
    return portalConfig.portalAddress();
  }

  /**
   * 切断字符串
   *
   * @param source 源
   * @return 超出部分用...来替换
   */
  private String cutOffString(String source) {
    if (source.length() > VALUE_MAX_LENGTH) {
      return source.substring(0, VALUE_MAX_LENGTH) + "...";
    }
    return source;
  }

}
