package com.ctrip.framework.apollo.portal.components.emailbuilder;


import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.Change;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.ReleaseService;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;


public abstract class ConfigPublishEmailBuilder {

  //email content common field placeholder
  private static final String EMAIL_CONTENT_FIELD_APPID = "\\$\\{appId\\}";
  private static final String EMAIL_CONTENT_FIELD_ENV = "\\$\\{env}";
  private static final String EMAIL_CONTENT_FIELD_CLUSTER = "\\$\\{clusterName}";
  private static final String EMAIL_CONTENT_FIELD_NAMESPACE = "\\$\\{namespaceName}";
  private static final String EMAIL_CONTENT_FIELD_OPERATOR = "\\$\\{operator}";
  private static final String EMAIL_CONTENT_FIELD_RELEASE_TIME = "\\$\\{releaseTime}";
  private static final String EMAIL_CONTENT_FIELD_RELEASE_ID = "\\$\\{releaseId}";
  private static final String EMAIL_CONTENT_FIELD_RELEASE_HISTORY_ID = "\\$\\{releaseHistoryId}";
  private static final String EMAIL_CONTENT_FIELD_RELEASE_TITLE = "\\$\\{releaseTitle}";
  private static final String EMAIL_CONTENT_FIELD_RELEASE_COMMENT = "\\$\\{releaseComment}";
  private static final String EMAIL_CONTENT_FIELD_APOLLO_SERVER_ADDRESS = "\\$\\{apollo.portal.address}";
  private static final String EMAIL_CONTENT_FIELD_DIFF = "\\$\\{diff}";

  //email content special field placeholder
  protected static final String EMAIL_CONTENT_FIELD_RULE = "\\$\\{rules}";

  //email content module switch
  private static final String EMAIL_CONTENT_DIFF_HAS_CONTENT_SWITCH = "diff-hidden";
  private static final String EMAIL_CONTENT_DIFF_HAS_NOT_CONTENT_SWITCH = "diff-empty-hidden";
  protected static final String EMAIL_CONTENT_RULE_SWITCH = "rule-hidden";

  //set config's value max length to protect email.
  protected static final int VALUE_MAX_LENGTH = 100;

  //email body template. config in db, so we can dynamic reject email content.
  private static final String EMAIL_TEMPLATE__RELEASE = "email.template.release";
  private static final String EMAIL_TEMPLATE__ROLLBACK = "email.template.rollback";

  private FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");


  private String emailSender;

  @Autowired
  private ServerConfigService serverConfigService;
  @Autowired
  private RolePermissionService rolePermissionService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private UserService userService;

  @PostConstruct
  public void init() {
    emailSender = serverConfigService.getValue("email.sender");
  }

  public Email build(Env env, ReleaseHistoryBO releaseHistory) {

    Email email = new Email();

    email.setSubject(subject());
    email.setSenderEmailAddress(emailSender);
    email.setBody(emailContent(env, releaseHistory));
    email.setRecipients(recipients(releaseHistory.getAppId(), releaseHistory.getNamespaceName()));

    return email;
  }

  protected abstract String subject();

  protected abstract String emailContent(Env env, ReleaseHistoryBO releaseHistory);

  private List<String> recipients(String appId, String namespaceName) {
    Set<UserInfo> modifyRoleUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.MODIFY_NAMESPACE));
    Set<UserInfo> releaseRoleUsers =
        rolePermissionService
            .queryUsersWithRole(RoleUtils.buildNamespaceRoleName(appId, namespaceName, RoleType.RELEASE_NAMESPACE));
    Set<UserInfo> owners = rolePermissionService.queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));

    Set<String> userIds = new HashSet<>(modifyRoleUsers.size() + releaseRoleUsers.size() + owners.size());

    for (UserInfo userInfo : modifyRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : releaseRoleUsers) {
      userIds.add(userInfo.getUserId());
    }

    for (UserInfo userInfo : owners) {
      userIds.add(userInfo.getUserId());
    }

    List<UserInfo> userInfos = userService.findByUserIds(Lists.newArrayList(userIds));

    if (CollectionUtils.isEmpty(userInfos)){
      return Collections.emptyList();
    }

    List<String> recipients = new ArrayList<>(userInfos.size());
    for (UserInfo userInfo: userInfos){
      recipients.add(userInfo.getEmail());
    }

    return recipients;
  }

  protected String renderEmailCommonContent(String template, Env env, ReleaseHistoryBO releaseHistory) {
    String renderResult = renderReleaseBasicInfo(template, env, releaseHistory);
    renderResult = renderDiffContent(renderResult, env, releaseHistory);
    return renderResult;
  }

  private String renderReleaseBasicInfo(String template, Env env, ReleaseHistoryBO releaseHistory) {
    String renderResult = template.replaceAll(EMAIL_CONTENT_FIELD_APPID, Matcher.quoteReplacement(releaseHistory.getAppId()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_ENV, Matcher.quoteReplacement(env.toString()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_CLUSTER, Matcher.quoteReplacement(releaseHistory.getClusterName()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_NAMESPACE, Matcher.quoteReplacement(releaseHistory.getNamespaceName()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_OPERATOR, Matcher.quoteReplacement(releaseHistory.getOperator()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_TITLE, Matcher.quoteReplacement(releaseHistory.getReleaseTitle()));
    renderResult =
        renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_ID, String.valueOf(releaseHistory.getReleaseId()));
    renderResult =
        renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_HISTORY_ID, String.valueOf(releaseHistory.getId()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_RELEASE_COMMENT, Matcher.quoteReplacement(releaseHistory.getReleaseComment()));
    renderResult = renderResult.replaceAll(EMAIL_CONTENT_FIELD_APOLLO_SERVER_ADDRESS, getApolloPortalAddress());
    return renderResult
        .replaceAll(EMAIL_CONTENT_FIELD_RELEASE_TIME, dateFormat.format(releaseHistory.getReleaseTime()));
  }

  private String renderDiffContent(String template, Env env, ReleaseHistoryBO releaseHistory) {
    String appId = releaseHistory.getAppId();
    String namespaceName = releaseHistory.getNamespaceName();

    AppNamespace appNamespace = appNamespaceService.findByAppIdAndName(appId, namespaceName);
    if (appNamespace == null) {
      appNamespace = appNamespaceService.findPublicAppNamespace(namespaceName);
    }

    //don't show diff content if namespace's format is file
    if (appNamespace == null ||
        !appNamespace.getFormat().equals(ConfigFileFormat.Properties.getValue())) {

      return template.replaceAll(EMAIL_CONTENT_FIELD_DIFF, "请点击链接到Apollo上查看");
    }

    ReleaseCompareResult result = getReleaseCompareResult(env, releaseHistory);

    if (!result.hasContent()) {
      String renderResult = template.replaceAll(EMAIL_CONTENT_DIFF_HAS_NOT_CONTENT_SWITCH, "");
      return renderResult.replaceAll(EMAIL_CONTENT_FIELD_DIFF, "");
    }

    List<Change> changes = result.getChanges();
    StringBuilder changesHtmlBuilder = new StringBuilder();
    for (Change change : changes) {
      String key = change.getEntity().getFirstEntity().getKey();
      String oldValue = change.getEntity().getFirstEntity().getValue();
      String newValue = change.getEntity().getSecondEntity().getValue();
      newValue = newValue == null ? "" : newValue;

      changesHtmlBuilder.append("<tr>");
      changesHtmlBuilder.append("<td width=\"10%\">").append(change.getType().toString()).append("</td>");
      changesHtmlBuilder.append("<td width=\"10%\">").append(cutOffString(key)).append("</td>");
      changesHtmlBuilder.append("<td width=\"10%\">").append(cutOffString(oldValue)).append("</td>");
      changesHtmlBuilder.append("<td width=\"10%\">").append(cutOffString(newValue)).append("</td>");

      changesHtmlBuilder.append("</tr>");
    }

    String diffContent = Matcher.quoteReplacement(changesHtmlBuilder.toString());
    String renderResult = template.replaceAll(EMAIL_CONTENT_FIELD_DIFF, diffContent);
    return renderResult.replaceAll(EMAIL_CONTENT_DIFF_HAS_CONTENT_SWITCH, "");
  }

  private ReleaseCompareResult getReleaseCompareResult(Env env, ReleaseHistoryBO releaseHistory) {
    if (releaseHistory.getOperation() == ReleaseOperation.GRAY_RELEASE
        && releaseHistory.getPreviousReleaseId() == 0) {
      ReleaseDTO masterLatestActiveRelease = releaseService.loadLatestRelease(
          releaseHistory.getAppId(), env, releaseHistory.getClusterName(), releaseHistory.getNamespaceName());
      ReleaseDTO branchLatestActiveRelease = releaseService.findReleaseById(env, releaseHistory.getReleaseId());

      return releaseService.compare(masterLatestActiveRelease, branchLatestActiveRelease);
    }

    return releaseService.compare(env, releaseHistory.getPreviousReleaseId(), releaseHistory.getReleaseId());
  }


  protected String getReleaseTemplate() {
    return serverConfigService.getValue(EMAIL_TEMPLATE__RELEASE);
  }

  protected String getRollbackTemplate() {
    return serverConfigService.getValue(EMAIL_TEMPLATE__ROLLBACK);
  }

  protected String getApolloPortalAddress() {
    return serverConfigService.getValue("apollo.portal.address");
  }

  private String cutOffString(String source) {
    if (source.length() > VALUE_MAX_LENGTH) {
      return source.substring(0, VALUE_MAX_LENGTH) + "...";
    }
    return source;
  }

}
