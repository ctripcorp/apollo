package com.ctrip.framework.apollo.portal.util;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * 相对的日期格式
 */
public class RelativeDateFormat {

  /**
   * 时间戳格式
   */
  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
  /**
   * 分钟毫秒数
   */
  private static final long ONE_MINUTE = 60000L;
  /**
   * 小时毫秒数
   */
  private static final long ONE_HOUR = 3600000L;
  /**
   * 天数毫秒数
   */
  private static final long ONE_DAY = 86400000L;

  private static final String ONE_SECOND_AGO = " seconds ago";
  private static final String ONE_MINUTE_AGO = " minutes ago";
  private static final String ONE_HOUR_AGO = " hours ago";
  private static final String ONE_DAY_AGO = " days ago";
  private static final String ONE_MONTH_AGO = " months ago";

  /**
   * 格式化为字符串
   *
   * @param date 指定的日期
   * @return 时间字符串
   */
  public static String format(Date date) {
    // 超出当前时间，返回now
    if (date.after(new Date())) {
      return "now";
    }

    long delta = System.currentTimeMillis() - date.getTime();
    // 距当前时间一分钟之内
    if (delta < ONE_MINUTE) {
      long seconds = toSeconds(delta);
      return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
    }
    // 距当前时间当前时间小于45分钟内
    if (delta < 45L * ONE_MINUTE) {
      long minutes = toMinutes(delta);
      return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
    }
    // 距当前时间当前时间小于24小时内
    if (delta < 24L * ONE_HOUR) {
      long hours = toHours(delta);
      return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
    }

    // 昨天
    Date lastDayBeginTime = getDateOffset(-1);
    if (date.after(lastDayBeginTime)) {
      return "yesterday";
    }

    // 前天
    Date lastTwoDaysBeginTime = getDateOffset(-2);
    if (date.after(lastTwoDaysBeginTime)) {
      return "the day before yesterday";
    }
    // 距当前时间当前时间小于30小时内
    if (delta < 30L * ONE_DAY) {
      long days = toDays(delta);
      return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
    }

    // 距当前时间当前时间3个月内
    long months = toMonths(delta);
    if (months <= 3) {
      return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
    }
    // 默认返回年-月-日
    return TIMESTAMP_FORMAT.format(date);
  }

  /**
   * 转换为秒
   *
   * @param date 时间戳
   * @return 转换后的秒数
   */
  private static long toSeconds(long date) {
    return date / 1000L;
  }

  /**
   * 转换为分钟数
   *
   * @param date 时间戳
   * @return 转换后的分钟数
   */
  private static long toMinutes(long date) {
    return toSeconds(date) / 60L;
  }

  /**
   * 转换为小时数
   *
   * @param date 时间戳
   * @return 转换后的小时数
   */
  private static long toHours(long date) {
    return toMinutes(date) / 60L;
  }

  /**
   * 转换为天
   *
   * @param date 时间戳
   * @return 转换后的天
   */
  private static long toDays(long date) {
    return toHours(date) / 24L;
  }

  /**
   * 转换为星期
   *
   * @param date 时间戳
   * @return 转换后的星期
   */
  private static long toMonths(long date) {
    return toDays(date) / 30L;
  }

  /**
   * 获取偏移后的时间
   *
   * @param offset 日期偏移的天数
   * @return 偏移后的日期
   */
  public static Date getDateOffset(int offset) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, offset);

    return getDayBeginTime(calendar.getTime());
  }

  /**
   * 指定日期开始的时间，即零晨
   *
   * @param date 指定日期
   * @return 指定日期开始的时间
   */
  private static Date getDayBeginTime(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return new Date(calendar.getTime().getTime());
  }

}
