package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.common.utils.PreferredUsernameUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
import com.ctrip.framework.apollo.portal.controller.ConfigsExportController;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * @author wxq
 */
public class NamespaceBOUtils {

  /**
   * namespace must not be {@link ConfigFileFormat#Properties}.
   * the content of namespace in item's value which item's key is {@link ConfigConsts#CONFIG_FILE_CONTENT_KEY}.
   * @param namespaceBO namespace
   * @return content of non-properties's namespace
   */
  static String convertNonProperties2configFileContent(NamespaceBO namespaceBO) {
    List<ItemBO> itemBOS = namespaceBO.getItems();
    for (ItemBO itemBO : itemBOS) {
      String key = itemBO.getItem().getKey();
      // special namespace format(not properties)
      if (ConfigConsts.CONFIG_FILE_CONTENT_KEY.equals(key)) {
        return itemBO.getItem().getValue();
      }
    }
    // If there is no items?
    // return empty string ""
    return "";
  }

  /**
   * copy from old {@link ConfigsExportController}.
   * convert {@link NamespaceBO} to a file content.
   * @return content of config file
   * @throws IllegalStateException if convert properties to string fail
   */
  public static String convert2configFileContent(NamespaceBO namespaceBO) {
    // early return if it is not a properties format namespace
    if (!ConfigFileFormat.Properties.equals(ConfigFileFormat.fromString(namespaceBO.getFormat()))) {
      // it is not a properties namespace
      return convertNonProperties2configFileContent(namespaceBO);
    }

    // it must be a properties format namespace
    List<ItemBO> itemBOS = namespaceBO.getItems();
    // save the kev value pair
    Properties properties = new Properties();
    for (ItemBO itemBO : itemBOS) {
      String key = itemBO.getItem().getKey();
      String value = itemBO.getItem().getValue();
      // ignore comment, so the comment will lack
      properties.put(key, value);
    }

    // use a special method convert properties to string
    final String configFileContent;
    try {
      configFileContent = PropertiesUtil.toString(properties);
    } catch (IOException e) {
      throw new IllegalStateException("convert properties to string fail.", e);
    }
    return configFileContent;
  }

  /**
   * enrich the preferred username for the namespace list
   *
   * @param namespaceList namespace list with operator id
   * @param repository    preferred username repository (operatorIdList -> preferredUsernameMap)
   */
  public static void enrichPreferredUserName(List<NamespaceBO> namespaceList,
      Function<List<String>, Map<String, String>> repository) {
    if (CollectionUtils.isEmpty(namespaceList)) {
      return;
    }
    Set<String> operatorIdSet = new HashSet<>();
    for (NamespaceBO namespace : namespaceList) {
      operatorIdSet.addAll(NamespaceBOUtils.extractOperatorId(namespace));
    }
    if (CollectionUtils.isEmpty(operatorIdSet)) {
      return;
    }
    // userId - preferredUsername
    Map<String, String> preferredUsernameMap = repository.apply(new ArrayList<>(operatorIdSet));
    if (CollectionUtils.isEmpty(preferredUsernameMap)) {
      return;
    }
    for (NamespaceBO namespace : namespaceList) {
      NamespaceBOUtils.setPreferredUsername(namespace, preferredUsernameMap);
    }
  }

  /**
   * enrich the preferred username for the namespace
   *
   * @param namespace  namespace with operator id
   * @param repository preferred username repository (operatorIdList -> preferredUsernameMap)
   */
  public static void enrichPreferredUserName(NamespaceBO namespace,
      Function<List<String>, Map<String, String>> repository) {
    if (namespace == null) {
      return;
    }
    Set<String> operatorIdSet = NamespaceBOUtils.extractOperatorId(namespace);
    if (CollectionUtils.isEmpty(operatorIdSet)) {
      return;
    }
    // userId - preferredUsername
    Map<String, String> preferredUsernameMap = repository.apply(new ArrayList<>(operatorIdSet));
    if (CollectionUtils.isEmpty(preferredUsernameMap)) {
      return;
    }
    NamespaceBOUtils.setPreferredUsername(namespace, preferredUsernameMap);
  }

  /**
   * extract operator id from the namespace
   *
   * @param namespace namespace with operator id
   * @return operator id set
   */
  public static Set<String> extractOperatorId(NamespaceBO namespace) {
    if (namespace == null) {
      return Collections.emptySet();
    }
    List<ItemBO> items = namespace.getItems();
    if (CollectionUtils.isEmpty(items)) {
      return Collections.emptySet();
    }
    return items.stream()
        .map(ItemBO::getItem)
        .map(PreferredUsernameUtil::extractOperatorId)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  /**
   * set the preferred username
   *
   * @param namespace            namespace with operator id
   * @param preferredUsernameMap (userId - preferredUsername) prepared preferred username map
   */
  public static void setPreferredUsername(NamespaceBO namespace,
      Map<String, String> preferredUsernameMap) {
    if (namespace == null) {
      return;
    }
    List<ItemBO> items = namespace.getItems();
    if (CollectionUtils.isEmpty(items)) {
      return;
    }
    items.forEach(itemBO -> PreferredUsernameUtil
        .setPreferredUsername(itemBO.getItem(), preferredUsernameMap));
  }
}
