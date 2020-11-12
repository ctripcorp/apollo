package com.ctrip.framework.apollo.util.yaml;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

/**
 * Yaml解析器， 移植自org.springframework.beans.factory.config.yaml解析器因为阿波罗不能直接依靠春天
 *
 * @since 1.3.0
 */
@Slf4j
public class YamlParser {

  private PropertiesFactory propertiesFactory = ApolloInjector.getInstance(PropertiesFactory.class);

  /**
   * 将yaml内容转换为 properties
   */
  public Properties yamlToProperties(String yamlContent) {
    Yaml yaml = createYaml();
    // properties实例
    final Properties result = propertiesFactory.getPropertiesInstance();
    // 逻辑处理
    process((properties, map) -> result.putAll(properties), yaml, yamlContent);
    return result;
  }

  /**
   * 创建要使用的{@link yaml}实例。
   *
   * @return yaml实例
   */
  private Yaml createYaml() {
    return new Yaml(new StrictMapAppenderConstructor());
  }

  /**
   * 处理
   *
   * @param callback 回调函数
   * @param yaml     yaml对象
   * @param content  内容字符串
   * @return 处理有效，true
   */
  private boolean process(MatchCallback callback, Yaml yaml, String content) {
    int count = 0;
    if (log.isDebugEnabled()) {
      log.debug("Loading from YAML: " + content);
    }
    // 遍历转换并计数
    for (Object object : yaml.loadAll(content)) {
      if (object != null && process(asMap(object), callback)) {
        count++;
      }
    }
    if (log.isDebugEnabled()) {
      log.debug(
          "Loaded " + count + " document" + (count > 1 ? "s" : "") + " from YAML resource: "
              + content);
    }
    return (count > 0);
  }

  /**
   * 将object转为Map对象
   *
   * @param object 对象实例
   * @return 转换后的Map对象
   */
  private Map<String, Object> asMap(Object object) {
    // YAML可以用数字作为键
    Map<String, Object> result = new LinkedHashMap<>();
    // 不为Map
    if (!(object instanceof Map)) {
      // 文档可以是文本文本
      result.put("document", object);
      return result;
    }

    // 强转为Map
    Map<Object, Object> map = (Map<Object, Object>) object;
    map.forEach((key, value) -> {
      // 如果值是Map,递归回调
      if (value instanceof Map) {
        value = asMap(value);
      }
      // 如果key为字符序列,调用toString
      if (key instanceof CharSequence) {
        result.put(key.toString(), value);
      } else {
        // 在这种情况下，它必须是一个Map键
        result.put("[" + key.toString() + "]", value);
      }
    });
    return result;
  }

  /**
   * 处理
   *
   * @param map      object转化后的Map
   * @param callback 回调方法
   * @return true, 处理成功
   */
  private boolean process(Map<String, Object> map, MatchCallback callback) {
    Properties properties = propertiesFactory.getPropertiesInstance();
    properties.putAll(getFlattenedMap(map));

    if (log.isDebugEnabled()) {
      log.debug("Merging document (no matchers set): " + map);
    }
    callback.process(properties, map);
    return true;
  }

  /**
   * 获取展开的Map
   *
   * @param source 源
   * @return 返回source展开后的Map
   */
  private Map<String, Object> getFlattenedMap(Map<String, Object> source) {
    Map<String, Object> result = new LinkedHashMap<>();
    buildFlattenedMap(result, source, null);
    return result;
  }

  /**
   * 构建展开的Map
   *
   * @param result 结果Map
   * @param source 源Map
   * @param path   路径（前缀）
   */
  private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source,
      String path) {
    // 遍历map
    source.forEach((key, value) -> {
      // 前缀
      if (StringUtils.isNotBlank(path)) {
        if (key.startsWith("[")) {
          key = path + key;
        } else {
          key = path + '.' + key;
        }
      }
      if (value instanceof String) {
        // 为String直接设置为key
        result.put(key, value);
      } else if (value instanceof Map) {
        // 合并
        Map<String, Object> map = (Map<String, Object>) value;
        // 递归
        buildFlattenedMap(result, map, key);
      } else if (value instanceof Collection) {
        // 合并，只不过key为下标
        Collection<Object> collection = (Collection<Object>) value;

        int count = 0;
        for (Object object : collection) {
          // 递归
          buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
        }
      } else {
        // 默认情况
        result.put(key, (value != null ? value.toString() : ""));
      }
    });
  }

  /**
   * 匹配回调函数
   */
  private interface MatchCallback {

    /**
     * 处理
     *
     * @param properties properties对象
     * @param map        map
     */
    void process(Properties properties, Map<String, Object> map);
  }

  /**
   * 一个专门的{@link SafeConstructor}，用于检查重复密钥。
   */
  private static class StrictMapAppenderConstructor extends SafeConstructor {

    // 声明为在子类中使用的公共
    StrictMapAppenderConstructor() {
      super();
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
      try {
        return super.constructMapping(node);
      } catch (IllegalStateException ex) {
        throw new ParserException("while parsing MappingNode", node.getStartMark(), ex.getMessage(),
            node.getEndMark());
      }
    }

    @Override
    protected Map<Object, Object> createDefaultMap() {
      final Map<Object, Object> delegate = super.createDefaultMap();
      return new AbstractMap<Object, Object>() {
        @Override
        public Object put(Object key, Object value) {
          // 存在，说明key重复了
          if (delegate.containsKey(key)) {
            throw new IllegalStateException("Duplicate key: " + key);
          }
          return delegate.put(key, value);
        }

        @Override
        public Set<Entry<Object, Object>> entrySet() {
          return delegate.entrySet();
        }
      };
    }
  }

}
