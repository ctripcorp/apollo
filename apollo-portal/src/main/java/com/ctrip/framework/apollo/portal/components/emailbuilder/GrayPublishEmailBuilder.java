package com.ctrip.framework.apollo.portal.components.emailbuilder;

import com.google.common.base.Joiner;
import com.google.gson.Gson;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

@Component
public class GrayPublishEmailBuilder extends ConfigPublishEmailBuilder {

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

  private String renderGrayReleaseRuleContent(String bodyTemplate, ReleaseHistoryBO releaseHistory) {

    Map<String, Object> context = releaseHistory.getOperationContext();
    Object rules = context.get("rules");
    List<GrayReleaseRuleItemDTO>
            ruleItems = rules == null ?
            null : gson.fromJson(rules.toString(), GsonType.RULE_ITEMS);


    if (CollectionUtils.isEmpty(ruleItems)) {
      return bodyTemplate.replaceAll(EMAIL_CONTENT_GRAY_RULES_MODULE, "<br><h4>无灰度规则</h4>");
    } else {
      StringBuilder rulesHtmlBuilder = new StringBuilder();
      for (GrayReleaseRuleItemDTO ruleItem : ruleItems) {
        String clientAppId = ruleItem.getClientAppId();
        Set<String> ips = ruleItem.getClientIpList();

        rulesHtmlBuilder.append("<b>AppId:&nbsp;</b>")
                .append(clientAppId)
                .append("&nbsp;&nbsp; <b>IP:&nbsp;</b>");

        IP_JOINER.appendTo(rulesHtmlBuilder, ips);
      }
      String grayRulesModuleContent = portalConfig.emailGrayRulesModuleTemplate().replaceAll(EMAIL_CONTENT_GRAY_RULES_CONTENT,
              Matcher.quoteReplacement(rulesHtmlBuilder.toString()));

      return bodyTemplate.replaceAll(EMAIL_CONTENT_GRAY_RULES_MODULE, Matcher.quoteReplacement(grayRulesModuleContent));
    }

  }
}
