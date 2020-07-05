package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.controller.ConfigsExportController;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wxq
 */
public class NamespaceBOUtils {

  /**
   * copy from old {@link ConfigsExportController}.
   * convert {@link NamespaceBO} to a file content.
   * @return content of config file
   */
  public static String convert2configFileContent(NamespaceBO namespaceBO) {
    List<String> fileItems = namespaceBO.getItems().stream().map(itemBO -> {
      String key = itemBO.getItem().getKey();
      String value = itemBO.getItem().getValue();

      // special namespace format
      if (ConfigConsts.CONFIG_FILE_CONTENT_KEY.equals(key)) {
        return value;
      }

      if ("".equals(key)) {
        return Joiner.on("").join(itemBO.getItem().getKey(), itemBO.getItem().getValue());
      }

      return Joiner.on(" = ").join(itemBO.getItem().getKey(), itemBO.getItem().getValue());
    }).collect(Collectors.toList());

    // merge every lines
    String configFileContent = fileItems.stream().collect(Collectors.joining(System.lineSeparator()));
    return configFileContent;
  }

}
