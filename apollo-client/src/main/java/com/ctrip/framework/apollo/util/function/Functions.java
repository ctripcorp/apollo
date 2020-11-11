package com.ctrip.framework.apollo.util.function;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.parser.ParserException;
import com.ctrip.framework.apollo.util.parser.Parsers;
import com.google.common.base.Function;
import java.util.Date;

/**
 * 函数接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Functions {

  /**
   * 转Int
   */
  Function<String, Integer> TO_INT_FUNCTION = input -> Integer.parseInt(input);

  /**
   * 转Long
   */
  Function<String, Long> TO_LONG_FUNCTION = input -> Long.parseLong(input);
  /**
   * 转Short
   */
  Function<String, Short> TO_SHORT_FUNCTION = input -> Short.parseShort(input);
  /**
   * 转Float
   */
  Function<String, Float> TO_FLOAT_FUNCTION = input -> Float.parseFloat(input);
  /**
   * 转Double
   */
  Function<String, Double> TO_DOUBLE_FUNCTION = input -> Double.parseDouble(input);
  /**
   * 转Byte
   */
  Function<String, Byte> TO_BYTE_FUNCTION = input -> Byte.parseByte(input);
  /**
   * 转Boolean
   */
  Function<String, Boolean> TO_BOOLEAN_FUNCTION = input -> Boolean.parseBoolean(input);
  /**
   * 转Date
   */
  Function<String, Date> TO_DATE_FUNCTION = input -> {
    Date result;
    try {
      result = Parsers.forDate().parse(input);
    } catch (ParserException ex) {
      throw new ApolloConfigException("Parse date failed", ex);
    }
    return result;
  };
  /**
   * 转成时毫秒数
   */
  Function<String, Long> TO_DURATION_FUNCTION = input -> {
    try {
      return Parsers.forDuration().parseToMillis(input);
    } catch (ParserException ex) {
      throw new ApolloConfigException("Parse duration failed", ex);
    }
  };
}
