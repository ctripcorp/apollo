package com.ctrip.framework.apollo.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ExceptionUtil {
  /**
   * Assemble the detail message for the throwable with all of its cause included (at most 10 causes).
   * @param ex the exception
   * @return the message along with its causes
   */
  public static String getDetailMessage(Throwable ex) {
    if (ex == null || Strings.isNullOrEmpty(ex.getMessage())) {
      return "";
    }
    StringBuilder builder = new StringBuilder(ex.getMessage());
    List<Throwable> causes = Lists.newLinkedList();

    int counter = 0;
    Throwable current = ex;
    //retrieve up to 10 causes
    while (current.getCause() != null && counter < 10) {
      Throwable next = current.getCause();
      causes.add(next);
      current = next;
      counter++;
    }

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
