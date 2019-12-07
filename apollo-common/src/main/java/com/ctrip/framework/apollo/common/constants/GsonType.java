package com.ctrip.framework.apollo.common.constants;

import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface GsonType {

  Type CONFIG = new TypeToken<Map<String, String>>() {}.getType();

  Type RULE_ITEMS = new TypeToken<List<GrayReleaseRuleItemDTO>>() {}.getType();

}
