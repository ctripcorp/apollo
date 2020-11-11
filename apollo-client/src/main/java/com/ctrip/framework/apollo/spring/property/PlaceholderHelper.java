package com.ctrip.framework.apollo.spring.property;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.Stack;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.StringUtils;

/**
 * 占位符帮助类.
 */
public class PlaceholderHelper {

  /**
   * 占位符前缀
   */
  private static final String PLACEHOLDER_PREFIX = "${";
  /**
   * 占位符后缀
   */
  private static final String PLACEHOLDER_SUFFIX = "}";
  /**
   * 值分隔符
   */
  private static final String VALUE_SEPARATOR = ":";
  /**
   * 常用的占位符前缀
   */
  private static final String SIMPLE_PLACEHOLDER_PREFIX = "{";
  /**
   * 表达式前缀
   */
  private static final String EXPRESSION_PREFIX = "#{";
  /**
   * 表达式后缀
   */
  private static final String EXPRESSION_SUFFIX = "}";

  /**
   * 解析占位符属性值，例如<br /> <br />“${somePropertyValue}”->“实际属性值”
   *
   * @param beanFactory bean工厂
   * @param beanName    bean的名称
   * @param placeholder 占位符，如@ApolloJsonValue("${jsonBeanProperty:[{'someString':'hello','someInt':100},{'someString':'world!','someInt':200}]}}")，即
   * @return 解析的值
   * @ApolloJsonValue的Value的占位符${jsonBeanProperty:[]}
   */
  public Object resolvePropertyValue(ConfigurableBeanFactory beanFactory, String beanName,
      String placeholder) {
    // 解析给定的占位符，例如注解属性。
    String strVal = beanFactory.resolveEmbeddedValue(placeholder);

    // 合并BeanDefinition
    BeanDefinition bd = (beanFactory.containsBean(beanName) ? beanFactory
        .getMergedBeanDefinition(beanName) : null);

    // 解析表达式,例如： "#{systemProperties.myProp}"
    return evaluateBeanDefinitionString(beanFactory, strVal, bd);
  }

  /**
   * 计算BeanDefinition字符串
   *
   * @param beanFactory    bean工厂
   * @param value          bean的名称
   * @param beanDefinition BeanDefinition对象，bean定义的信息
   * @return 解析后的value
   */
  private Object evaluateBeanDefinitionString(ConfigurableBeanFactory beanFactory, String value,
      BeanDefinition beanDefinition) {
    // 如果bean的表达式解析器为空，跳过
    if (beanFactory.getBeanExpressionResolver() == null) {
      return value;
    }

    // 实例的范围
    Scope scope = (beanDefinition != null ? beanFactory
        .getRegisteredScope(beanDefinition.getScope()) : null);
    // 解析表达式
    return beanFactory.getBeanExpressionResolver()
        .evaluate(value, new BeanExpressionContext(beanFactory, scope));
  }

  /**
   * 从占位符中提取keys，例如：
   * <ul>
   * <li>${some.key} => "some.key"</li>
   * <li>${some.key:${some.other.key:100}} => "some.key", "some.other.key"</li>
   * <li>${${some.key}} => "some.key"</li>
   * <li>${${some.key:other.key}} => "some.key"</li>
   * <li>${${some.key}:${another.key}} => "some.key", "another.key"</li>
   * <li>#{new java.text.SimpleDateFormat('${some.key}').parse('${another.key}')} => "some.key", "another.key"</li>
   * </ul>
   *
   * @param propertyString 占位符字符串
   * @return 占位符Key列表
   */
  public Set<String> extractPlaceholderKeys(String propertyString) {
    // 占位符的key列表
    Set<String> placeholderKeys = Sets.newHashSet();

    // 为空，不包含占位符或者表达式占位符，跳过
    if (Strings.isNullOrEmpty(propertyString) || (!isNormalizedPlaceholder(propertyString)
        && !isExpressionWithPlaceholder(propertyString))) {
      return placeholderKeys;
    }

    // 堆
    Stack<String> stack = new Stack<>();
    stack.push(propertyString);

    while (!stack.isEmpty()) {
      String strVal = stack.pop();
      // 开始的下标
      int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
      // 没有找到PLACEHOLDER_PREFIX，直接添加，然后跳过，因为这里它是一个没有占位符的值
      if (startIndex == -1) {
        placeholderKeys.add(strVal);
        continue;
      }
      // 结束的下标
      int endIndex = findPlaceholderEndIndex(strVal, startIndex);
      // 无效的占位符
      if (endIndex == -1) {
        // invalid placeholder?
        continue;
      }

      // 除去占位符的字符串
      String placeholderCandidate = strVal
          .substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);

      // 如果还存在表达式，添加到堆中
      // ${some.key:other.key}
      if (placeholderCandidate.startsWith(PLACEHOLDER_PREFIX)) {
        stack.push(placeholderCandidate);
      } else {
        // 值分隔符
        // some.key:${some.other.key:100}
        int separatorIndex = placeholderCandidate.indexOf(VALUE_SEPARATOR);

        // 不存在值分隔符，添加到堆中
        if (separatorIndex == -1) {
          stack.push(placeholderCandidate);
        } else {
          // 添加key
          stack.push(placeholderCandidate.substring(0, separatorIndex));
          // 返回存在占位符中的值
          String defaultValuePart = normalizeToPlaceholder(
              placeholderCandidate.substring(separatorIndex + VALUE_SEPARATOR.length()));
          // 不为空添加至堆中
          if (!Strings.isNullOrEmpty(defaultValuePart)) {
            stack.push(defaultValuePart);
          }
        }
      }

      // has remaining part, e.g. ${a}.${b}
      // 判断占位符结束后面是否还有尾巴
      if (endIndex + PLACEHOLDER_SUFFIX.length() < strVal.length() - 1) {
        // 剩余片段
        String remainingPart = normalizeToPlaceholder(
            strVal.substring(endIndex + PLACEHOLDER_SUFFIX.length()));
        // 不为空添加至堆中
        if (!Strings.isNullOrEmpty(remainingPart)) {
          stack.push(remainingPart);
        }
      }
    }

    return placeholderKeys;
  }

  /**
   * 是否为正常的占位符
   *
   * @param propertyString 占位符字符串
   * @return true, 是正常的占位符，否则，false
   */
  private boolean isNormalizedPlaceholder(String propertyString) {
    return propertyString.startsWith(PLACEHOLDER_PREFIX) && propertyString
        .contains(PLACEHOLDER_SUFFIX);
  }

  /**
   * 是否为表达式占位符
   *
   * @param propertyString 表达式占位符字符串
   * @return true, 是表达式占位符，否则，false
   */
  private boolean isExpressionWithPlaceholder(String propertyString) {
    return propertyString.startsWith(EXPRESSION_PREFIX)
        && propertyString.contains(EXPRESSION_SUFFIX) && propertyString.contains(PLACEHOLDER_PREFIX)
        && propertyString.contains(PLACEHOLDER_SUFFIX);
  }

  /**
   * 标准化占位符
   *
   * @param strVal 占位符的值
   * @return 返回占位符值中的占位符信息，如 strVal = "${batch:100}"，返回${batch:100}
   */
  private String normalizeToPlaceholder(String strVal) {
    // 开始占位符下标
    int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
    if (startIndex == -1) {
      return null;
    }
    // 结束占位符下标
    int endIndex = strVal.lastIndexOf(PLACEHOLDER_SUFFIX);
    if (endIndex == -1) {
      return null;
    }
    // strVal = "${batch:100}"，返回${batch:100}
    return strVal.substring(startIndex, endIndex + PLACEHOLDER_SUFFIX.length());
  }

  /**
   * 查找占位符
   *
   * @param buf        字符序列，如“${batch:100}”
   * @param startIndex 开始下标
   * @return 占符符的下标，-1表示没有找到
   */
  private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
    // 起始下标，不包括占位符
    int index = startIndex + PLACEHOLDER_PREFIX.length();
    // 嵌套占位符
    int withinNestedPlaceholder = 0;

    while (index < buf.length()) {
      // 判断结束符是否为PLACEHOLDER_SUFFIX
      if (StringUtils.substringMatch(buf, index, PLACEHOLDER_SUFFIX)) {

        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + PLACEHOLDER_SUFFIX.length();
        } else {
          // 返回占位符下标
          return index;
        }
      } else if (StringUtils.substringMatch(buf, index, SIMPLE_PLACEHOLDER_PREFIX)) {
        // 如果有嵌套占位符，加上占位符的长度
        withinNestedPlaceholder++;
        index = index + SIMPLE_PLACEHOLDER_PREFIX.length();
      } else {
        // 默认下标加1
        index++;
      }
    }

    return -1;
  }
}
