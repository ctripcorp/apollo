package com.ctrip.framework.apollo.common.constants;

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Gson类型
 */
public interface GsonType {

  /**
   * 属性配置项类型
   */
  Type CONFIG = new TypeToken<Map<String, String>>() {
  }.getType();
  /**
   * 灰度发布规则明细类型
   */
  Type RULE_ITEMS = new TypeToken<List<GrayReleaseRuleItemDTO>>() {
  }.getType();

}
