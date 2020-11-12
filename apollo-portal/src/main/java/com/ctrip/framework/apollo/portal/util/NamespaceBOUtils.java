package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
import com.ctrip.framework.apollo.portal.controller.ConfigsExportController;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * 名称空间业务对象工具类
 *
 * @author wxq
 */
public class NamespaceBOUtils {

  /**
   * 转换不为Properties的配置文件内容，名称空间不能是 {@link ConfigFileFormat#Properties}. 名称空间中配置项的键值为{@link
   * ConfigConsts#CONFIG_FILE_CONTENT_KEY}.的名称空间的内容。
   *
   * @param namespaceBO 名称空间业务对象
   * @return 非属性名称空间的内容
   */
  static String convertNonProperties2configFileContent(NamespaceBO namespaceBO) {
    List<ItemBO> itemBOS = namespaceBO.getItems();
    for (ItemBO itemBO : itemBOS) {
      String key = itemBO.getItem().getKey();
      // 特定的名称空间格式(not properties)
      if (ConfigConsts.CONFIG_FILE_CONTENT_KEY.equals(key)) {
        return itemBO.getItem().getValue();
      }
    }
    // 如果没有配置项，返回空字符串
    return "";
  }

  /**
   * 转换配置文件内容，复制于旧的{@link ConfigsExportController}. 转换 {@link NamespaceBO} 至一个文件内容.
   *
   * @param namespaceBO 名称空间业务对象
   * @return 配置文件的内容
   * @throws IllegalStateException if convert properties to string fail
   */
  public static String convert2configFileContent(NamespaceBO namespaceBO) {
    // 如果不是属性格式的名称空间，则提前返回
    if (!ConfigFileFormat.Properties.equals(ConfigFileFormat.fromString(namespaceBO.getFormat()))) {
      // not properties名称空间
      return convertNonProperties2configFileContent(namespaceBO);
    }

    // 它必须是一个属性格式的名称空间
    List<ItemBO> itemBOS = namespaceBO.getItems();
    // 保存键值对
    Properties properties = new Properties();
    for (ItemBO itemBO : itemBOS) {
      String key = itemBO.getItem().getKey();
      String value = itemBO.getItem().getValue();
      // 忽略注释，这样就会缺少注释
      properties.put(key, value);
    }

    // 使用特殊方法将属性转换为字符串
    final String configFileContent;
    try {
      configFileContent = PropertiesUtil.toString(properties);
    } catch (IOException e) {
      throw new IllegalStateException("convert properties to string fail.", e);
    }
    return configFileContent;
  }

}
