package com.ctrip.framework.apollo.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * 异常工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ExceptionUtil {

  /**
   * 收集throwable所有的详细信息，包括其所有原因（最多10个原因）
   *
   * @param ex 异常实例
   * @return 异常消息及其原因
   */
  public static String getDetailMessage(Throwable ex) {
    // 为空，返回空字符串
    if (ex == null || Strings.isNullOrEmpty(ex.getMessage())) {
      return "";
    }
    StringBuilder builder = new StringBuilder(ex.getMessage());
    // 原因列表
    List<Throwable> causes = Lists.newLinkedList();
    // 个数
    int counter = 0;
    Throwable current = ex;
    // 最多找到10个原因
    while (current.getCause() != null && counter < 10) {
      Throwable next = current.getCause();
      causes.add(next);
      current = next;
      counter++;
    }

    // 过滤空的原因，追加原因的信息
    for (Throwable cause : causes) {
      if (Strings.isNullOrEmpty(cause.getMessage())) {
        counter--;
        continue;
      }
      builder.append(" [Cause: ").append(cause.getMessage());
    }

    builder.append(Strings.repeat("]", counter));

    return builder.toString();
  }
}
