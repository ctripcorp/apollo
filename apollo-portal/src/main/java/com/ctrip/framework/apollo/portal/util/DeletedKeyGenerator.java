package com.ctrip.framework.apollo.portal.util;

/**
 * 删除AppId时生成新的AppId
 */
public class DeletedKeyGenerator {

  public static String generate(String oldApp) {
    return String.format("%s_%s_%s", "DELETED", oldApp, System.currentTimeMillis());
  }
}
