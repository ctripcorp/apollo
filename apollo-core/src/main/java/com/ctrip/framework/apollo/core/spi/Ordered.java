package com.ctrip.framework.apollo.core.spi;

/**
 * {@code Ordered}是一个接口，可以由应该是 <em>orderable</em>的对象实现，例如 {@code Collection}.
 *
 * <p>实际{@link #getOrder() order}可以解释为优先级排序，第一个对象（具有最低的顺序值）具有最高优先级。
 *
 * <p>
 *
 * @author Jason Song(song_s@ctrip.com)
 * @since 1.0.0
 */
public interface Ordered {

  /**
   * 用于最高优先级值的有用常量.
   *
   * @see java.lang.Integer#MIN_VALUE
   */
  int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

  /**
   * 最小优先级值的有用常量.
   *
   * @see java.lang.Integer#MAX_VALUE
   */
  int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


  /**
   * 获取此对象的优先级值。
   * <p>值越高，优先级越低。因此，值最小的对象具有最高的优先级（有点类似于{@code load-on-startup}Servlet值）。
   * <p>相同的顺序值将导致受影响对象的任意排序位置。
   *
   * @return order值
   * @see #HIGHEST_PRECEDENCE
   * @see #LOWEST_PRECEDENCE
   */
  int getOrder();
}
