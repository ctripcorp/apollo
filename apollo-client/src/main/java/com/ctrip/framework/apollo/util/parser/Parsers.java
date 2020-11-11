package com.ctrip.framework.apollo.util.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间解析集工具类
 */
public class Parsers {

  /**
   * 获取日期解析器
   *
   * @return 日期解析器实例
   */
  public static DateParser forDate() {
    return DateParser.INSTANCE;
  }

  /**
   * 获取秒或纳秒时间间隔解析器解析器
   *
   * @return 秒或纳秒时间间隔解析器实例
   */
  public static DurationParser forDuration() {
    return DurationParser.INSTANCE;
  }

  /**
   * 日期解析器
   */
  public enum DateParser {
    /**
     * 实例对象
     */
    INSTANCE;

    /**
     * 长日期格式
     */
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * 中号日期格式
     */
    private static final String MEDIUM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 短日期格式
     */
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 将尝试解析日期使用Locale.US格式，如下：yyyy-MM-dd HH:mm:ss.SSS, yyyy-MM-dd HH:mm:ss和yyyy-MM-dd
     *
     * @param text 解析的文本
     * @return 解析后的日期
     * @throws ParserException 如果文本无法解析，抛出
     */
    public Date parse(String text) throws ParserException {
      text = text.trim();
      int length = text.length();

      // 匹配日期文本
      if (length == LONG_DATE_FORMAT.length()) {
        return parse(text, LONG_DATE_FORMAT);
      }

      if (length == MEDIUM_DATE_FORMAT.length()) {
        return parse(text, MEDIUM_DATE_FORMAT);
      }

      return parse(text, SHORT_DATE_FORMAT);
    }

    /**
     * 使用指定的格式和Locale.US解析文本
     *
     * @param text   解析的文本
     * @param format 日期格式, 更多信息请看 {@link java.text.SimpleDateFormat}
     * @return 解析后的日期
     * @throws ParserException 如果文本无法解析，抛出
     */
    public Date parse(String text, String format) throws ParserException {
      return parse(text, format, Locale.US);
    }

    /**
     * 使用指定的格式和locale解析文本
     *
     * @param text   解析的文本
     * @param format 日期格式, 更多信息请看 {@link java.text.SimpleDateFormat}
     * @param locale 区域设置
     * @return 解析后的日期
     * @throws ParserException 如果文本无法解析，抛出
     */
    public Date parse(String text, String format, Locale locale) throws ParserException {
      SimpleDateFormat dateFormat = getDateFormat(format, locale);

      try {
        return dateFormat.parse(text.trim());
      } catch (ParseException e) {
        throw new ParserException(
            "Error when parsing date(" + dateFormat.toPattern() + ") from " + text, e);
      }
    }

    /**
     * 获取日期格式化对象
     *
     * @param format 日期格式, 更多信息请看 {@link java.text.SimpleDateFormat}
     * @param locale 区域设置
     * @return SimpleDateFormat对象实例
     */
    private SimpleDateFormat getDateFormat(String format, Locale locale) {
      return new SimpleDateFormat(format, locale);
    }
  }

  /**
   * 秒或纳秒时间间隔解析器，适合处理较短的时间，需要更高的精确性
   */
  public enum DurationParser {
    /**
     * 实例对象
     */
    INSTANCE;

    /**
     * 正则表达式
     */
    private static final Pattern PATTERN = Pattern
        .compile("(?:([0-9]+)D)?(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?(?:([0-9]+)(?:MS)?)?",
            Pattern.CASE_INSENSITIVE);
    /**
     * 每天的小时数
     */
    private static final int HOURS_PER_DAY = 24;
    /**
     * 每小时的分钟数
     */
    private static final int MINUTES_PER_HOUR = 60;
    /**
     * 每分钟的秒数
     */
    private static final int SECONDS_PER_MINUTE = 60;
    /**
     * 每秒的毫秒数
     */
    private static final int MILLIS_PER_SECOND = 1000;
    /**
     * 每分钟的毫秒数
     */
    private static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    /**
     * 每小时的毫秒数
     */
    private static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * 每天的毫秒数
     */
    private static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;

    /**
     * 解析文本并返回毫秒数
     *
     * @param text 文本
     * @return 解析后的毫秒数
     * @throws ParserException 如果文本无法解析，抛出
     */
    public long parseToMillis(String text) throws ParserException {
      Matcher matcher = PATTERN.matcher(text);
      if (matcher.matches()) {
        String dayMatch = matcher.group(1);
        String hourMatch = matcher.group(2);
        String minuteMatch = matcher.group(3);
        String secondMatch = matcher.group(4);
        String fractionMatch = matcher.group(5);
        if (dayMatch != null || hourMatch != null || minuteMatch != null || secondMatch != null
            || fractionMatch != null) {
          int daysAsMilliSecs = parseNumber(dayMatch, MILLIS_PER_DAY);
          int hoursAsMilliSecs = parseNumber(hourMatch, MILLIS_PER_HOUR);
          int minutesAsMilliSecs = parseNumber(minuteMatch, MILLIS_PER_MINUTE);
          int secondsAsMilliSecs = parseNumber(secondMatch, MILLIS_PER_SECOND);
          int milliseconds = parseNumber(fractionMatch, 1);
          // 解析后的毫秒数
          return daysAsMilliSecs + hoursAsMilliSecs + minutesAsMilliSecs + secondsAsMilliSecs
              + milliseconds;
        }
      }
      throw new ParserException(String.format("Text %s cannot be parsed to duration)", text));
    }

    /**
     * 解析数字
     *
     * @param parsed     解析的值
     * @param multiplier 乘数
     * @return 解析后的数字
     */
    private static int parseNumber(String parsed, int multiplier) {
      // regex limits to [0-9]+
      if (parsed == null || parsed.trim().isEmpty()) {
        return 0;
      }
      return Integer.parseInt(parsed) * multiplier;
    }
  }
}
