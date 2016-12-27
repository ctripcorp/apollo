package com.ctrip.framework.apollo.portal.util;

import org.apache.commons.lang.time.FastDateFormat;

import java.util.Date;


public class RelativeDateFormat {
  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
  private static final long ONE_MINUTE = 60000L;
  private static final long ONE_HOUR = 3600000L;
  private static final long ONE_DAY = 86400000L;

  private static final String ONE_SECOND_AGO = "秒前";
  private static final String ONE_MINUTE_AGO = "分钟前";
  private static final String ONE_HOUR_AGO = "小时前";
  private static final String ONE_DAY_AGO = "天前";
  private static final String ONE_MONTH_AGO = "月前";

  public static String format(Date date) {
    if (date.after(new Date())) {
      return "now";
    }

    long delta = new Date().getTime() - date.getTime();
    if (delta < ONE_MINUTE) {
      long seconds = toSeconds(delta);
      return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
    }
    if (delta < 45L * ONE_MINUTE) {
      long minutes = toMinutes(delta);
      return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
    }
    if (delta < 24L * ONE_HOUR) {
      long hours = toHours(delta);
      return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
    }
    if (delta < 48L * ONE_HOUR) {
      return "昨天";
    }
    if (delta < 30L * ONE_DAY) {
      long days = toDays(delta);
      return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
    }

    long months = toMonths(delta);
    if (months <= 3) {
      return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
    } else {
      return TIMESTAMP_FORMAT.format(date);
    }
  }

  private static long toSeconds(long date) {
    return date / 1000L;
  }

  private static long toMinutes(long date) {
    return toSeconds(date) / 60L;
  }

  private static long toHours(long date) {
    return toMinutes(date) / 60L;
  }

  private static long toDays(long date) {
    return toHours(date) / 24L;
  }

  private static long toMonths(long date) {
    return toDays(date) / 30L;
  }

}
