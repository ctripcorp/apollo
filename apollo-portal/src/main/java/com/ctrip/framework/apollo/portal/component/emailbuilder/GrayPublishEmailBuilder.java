package com.ctrip.framework.apollo.portal.component.emailbuilder;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 灰度发布邮箱构建器
 */
@Component
public class GrayPublishEmailBuilder extends ConfigPublishEmailBuilder {

  /**
   * 邮箱主体
   */
  private static final String EMAIL_SUBJECT = "[Apollo] 灰度发布";

  private Gson gson = new Gson();
  private Joiner IP_JOINER = Joiner.on(", ");

  @Override
  protected String subject() {
    return EMAIL_SUBJECT;
  }

  @Override
  public String emailContent(Env env, ReleaseHistoryBO releaseHistory) {
    String result = renderEmailCommonContent(env, releaseHistory);
    return renderGrayReleaseRuleContent(result, releaseHistory);
  }

  @Override
  protected String getTemplateFramework() {
    return portalConfig.emailTemplateFramework();
  }

  @Override
  protected String getDiffModuleTemplate() {
    return portalConfig.emailReleaseDiffModuleTemplate();
  }

  /**
   * 提取灰色的发布规则内容
   *
   * @param bodyTemplate   body模板
   * @param releaseHistory 发布历史信息
   * @return
   */
  private String renderGrayReleaseRuleContent(String bodyTemplate,
      ReleaseHistoryBO releaseHistory) {

    // 操作类型内容
    Map<String, Object> context = releaseHistory.getOperationContext();
    Object rules = context.get("rules");
    // 发布规则配置项信息列表
    List<GrayReleaseRuleItemDTO> ruleItems = rules == null ? null : gson.fromJson(rules.toString(),
        GsonType.RULE_ITEMS);

    if (CollectionUtils.isEmpty(ruleItems)) {
      return bodyTemplate.replaceAll(EMAIL_CONTENT_GRAY_RULES_MODULE, "<br><h4>无灰度规则</h4>");
    }

    // 构建规则html
    StringBuilder rulesHtmlBuilder = new StringBuilder();
    for (GrayReleaseRuleItemDTO ruleItem : ruleItems) {
      String clientAppId = ruleItem.getClientAppId();
      Set<String> ips = ruleItem.getClientIpList();

      rulesHtmlBuilder.append("<b>AppId:&nbsp;</b>")
          .append(clientAppId)
          .append("&nbsp;&nbsp; <b>IP:&nbsp;</b>");

      IP_JOINER.appendTo(rulesHtmlBuilder, ips);
    }

    // 设置灰度发布的灰度规则模块模板
    String grayRulesModuleContent = portalConfig.emailGrayRulesModuleTemplate().replaceAll(
        EMAIL_CONTENT_GRAY_RULES_CONTENT, Matcher.quoteReplacement(rulesHtmlBuilder.toString()));

    return bodyTemplate.replaceAll(EMAIL_CONTENT_GRAY_RULES_MODULE,
        Matcher.quoteReplacement(grayRulesModuleContent));

  }
}
