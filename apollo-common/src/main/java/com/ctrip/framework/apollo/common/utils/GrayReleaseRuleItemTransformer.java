package com.ctrip.framework.apollo.common.utils;

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * 灰度发布规则属性配置项转换
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class GrayReleaseRuleItemTransformer {

  /**
   * Gson对象
   */
  private static final Gson gson = new Gson();
  /**
   * 灰度发布规则属性配置项类型
   */
  private static final Type grayReleaseRuleItemsType = new TypeToken<Set<GrayReleaseRuleItemDTO>>() {
  }.getType();

  /**
   * 将json字符串转换为灰度发布规则明细列表
   *
   * @param content 内容JSON
   * @return 灰度发布规则明细列表
   */
  public static Set<GrayReleaseRuleItemDTO> batchTransformFromJSON(String content) {
    return gson.fromJson(content, grayReleaseRuleItemsType);
  }

  /**
   * 灰度发布规则明细列表转换为json字符串
   *
   * @param ruleItems 灰度发布规则明细列表
   * @return json字符串
   */
  public static String batchTransformToJSON(Set<GrayReleaseRuleItemDTO> ruleItems) {
    return gson.toJson(ruleItems);
  }
}
